package androidx.lifecycle;

public class EnhancedMutableLiveData<T> extends EnhancedLiveData<T> {

    public EnhancedMutableLiveData(T value) {
        super(value);
    }

    public EnhancedMutableLiveData() {
        super();
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