package androidx.lifecycle;

import static androidx.lifecycle.Lifecycle.State.STARTED;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.android.base.foundation.livedata.SingleLiveData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import timber.log.Timber;

public class EnhancedLiveData<T> extends LiveData<T> {

    private int mVersion = START_VERSION;
    private final boolean mAsSingleLiveData;

    private final List<ObserverWrapper<? super T>> mWrapperObserverList = new ArrayList<>();

    public EnhancedLiveData(boolean asSingleLiveData, T value) {
        super(value);
        mAsSingleLiveData = asSingleLiveData;
    }

    public EnhancedLiveData(boolean asSingleLiveData) {
        super();
        mAsSingleLiveData = asSingleLiveData;
    }

    @Override
    protected void setValue(T value) {
        mVersion++;
        super.setValue(value);
    }

    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        observe(owner, STARTED, observer);
    }

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, Lifecycle.State activeState, @NonNull Observer<? super T> observer) {
        if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            // ignore
            return;
        }
        observer = getOrNewWrappedObserver(observer, mVersion);
        try {
            //use ExternalLifecycleBoundObserver instead of LifecycleBoundObserver
            LifecycleBoundObserver wrapper = new ExternalLifecycleBoundObserver(owner, activeState, observer);
            //noinspection unchecked
            LifecycleBoundObserver existing = (LifecycleBoundObserver) callMethodPutIfAbsent(observer, wrapper);
            if (existing != null && !existing.isAttachedTo(owner)) {
                throw new IllegalArgumentException("Cannot add the same observer" + " with different lifecycles");
            }
            if (existing != null) {
                return;
            }
            owner.getLifecycle().addObserver(wrapper);
        } catch (Exception e) {
            Timber.e(e, "observe: ");
        }
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        super.observeForever(getOrNewWrappedObserver(observer, mVersion));
    }

    class ExternalLifecycleBoundObserver extends LifecycleBoundObserver {

        private final Lifecycle.State mActiveState;


        ExternalLifecycleBoundObserver(
                @NonNull LifecycleOwner owner,
                Lifecycle.State activeState,
                Observer<? super T> observer
        ) {
            super(owner, observer);
            mActiveState = activeState;
        }

        @Override
        boolean shouldBeActive() {
            return mOwner.getLifecycle().getCurrentState().isAtLeast(mActiveState);
        }
    }

    private Object getFieldObservers() throws Exception {
        Field fieldObservers = LiveData.class.getDeclaredField("mObservers");
        fieldObservers.setAccessible(true);
        return fieldObservers.get(this);
    }

    private Object callMethodPutIfAbsent(Object observer, Object wrapper) throws Exception {
        Object mObservers = getFieldObservers();
        Class<?> classOfSafeIterableMap = mObservers.getClass();
        Method putIfAbsent = classOfSafeIterableMap.getDeclaredMethod("putIfAbsent", Object.class, Object.class);
        putIfAbsent.setAccessible(true);
        return putIfAbsent.invoke(mObservers, observer, wrapper);
    }


    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        ObserverWrapper<? super T> wrapper = findWrapper(observer);
        super.removeObserver(wrapper);
        mWrapperObserverList.remove(wrapper);
    }

    private ObserverWrapper<? super T> getOrNewWrappedObserver(@NonNull Observer<? super T> observer, int observerVersion) {
        ObserverWrapper<? super T> wrapper = findWrapper(observer);

        if (wrapper == null) {
            wrapper = new ObserverWrapper<>(observerVersion, observer);
            mWrapperObserverList.add(wrapper);
        }

        return wrapper;
    }

    private ObserverWrapper<? super T> findWrapper(Observer<? super T> observer) {
        ListIterator<ObserverWrapper<? super T>> iterator = mWrapperObserverList.listIterator();

        ObserverWrapper<? super T> target = null;

        while (iterator.hasNext()) {
            ObserverWrapper<? super T> next = iterator.next();
            if (next.mOrigin == observer) {
                target = next;
                Timber.d("findWrapper next.mOrigin == observer");
                break;
            }
            if (next == observer) {
                Timber.d("findWrapper next == observer");
                target = next;
                break;
            }
        }

        return target;
    }

    private class ObserverWrapper<E> implements Observer<E> {

        private final int mObserverVersion;

        private final Observer<E> mOrigin;

        private ObserverWrapper(int observerVersion, Observer<E> origin) {
            mObserverVersion = observerVersion;
            mOrigin = origin;
        }

        @Override
        public void onChanged(@Nullable E t) {
            if ((!mAsSingleLiveData || mObserverVersion < mVersion) && mOrigin != null) {
                mOrigin.onChanged(t);
            }
        }
    }

}