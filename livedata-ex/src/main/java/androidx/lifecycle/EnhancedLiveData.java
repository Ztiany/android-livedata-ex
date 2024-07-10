package androidx.lifecycle;

import static androidx.lifecycle.Lifecycle.State.STARTED;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import timber.log.Timber;

public class EnhancedLiveData<T> extends LiveData<T> {

    public EnhancedLiveData(T value) {
        super(value);
    }

    public EnhancedLiveData() {
        super();
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

}