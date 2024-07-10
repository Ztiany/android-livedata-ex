package androidx.lifecycle;

public class EnhancedMutableLiveData<T> extends EnhancedLiveData<T> {

    public EnhancedMutableLiveData(boolean asSingleLiveData, T value) {
        super(asSingleLiveData, value);
    }

    public EnhancedMutableLiveData(boolean asSingleLiveData) {
        super(asSingleLiveData);
    }

    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }

}