package com.example.module_alarm.fcm;

import com.example.module_utility.response.Response;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class FcmService {

    private final FcmRepository fcmRepository;
    @Value("${FIREBASE_KEY}")
    private String serviceAccountKeyPath;

    @PostConstruct
    public void initFirebase() throws IOException {
        log.info(serviceAccountKeyPath);
        FileInputStream serviceAccount = new FileInputStream(serviceAccountKeyPath);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
    }

    public Response<Void> saveToken(FcmTokenDTO request){
        Optional<Fcm> existingFcm = fcmRepository.findByToken(request.getToken());
        if(existingFcm.isPresent()){
            return Response.error(409, "Token already exists");
        }
        Fcm fcm = request.toEntity();
        fcmRepository.save(fcm);
        return Response.successWithoutData();
    }


}
