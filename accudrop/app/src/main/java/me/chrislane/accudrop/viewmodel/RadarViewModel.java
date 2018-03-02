package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Date;
import java.util.UUID;


public class RadarViewModel extends ViewModel {
    private MutableLiveData<UUID> subject = new MutableLiveData<>();
    private MutableLiveData<Date> subjectTime = new MutableLiveData<>();

    public LiveData<UUID> getSubject() {
        return subject;
    }

    public void setSubject(UUID subject) {
        this.subject.setValue(subject);
    }

    public LiveData<Date> getSubjectTime() {
        return subjectTime;
    }

    public void setSubjectTime(Date subjectTime) {
        this.subjectTime.setValue(subjectTime);
    }
}
