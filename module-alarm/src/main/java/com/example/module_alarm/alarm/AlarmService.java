package com.example.module_alarm.alarm;

import com.example.module_alarm.fcm.Fcm;
import com.example.module_alarm.fcm.FcmRepository;
import com.example.module_utility.response.Response;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AlarmService {
    private final AlarmRepository alarmRepository;
    private final FcmRepository fcmRepository;

    public Response<List<AlarmResponseDTO>> findAlarmByUserId(Integer userId){
        List<AlarmResponseDTO> alarms = alarmRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(AlarmResponseDTO::toDTO)
                .collect(Collectors.toList());
        return Response.success(alarms);
    }

    @Transactional
    public void sendAlarm(AlarmRequestDTO requestDTO) {
        //alarm 내역 저장
        alarmRepository.save(requestDTO.toEntity());
        //fcm 푸시 알림
        List<Fcm> fcmList = fcmRepository.findByUserId(requestDTO.getUserId());
        String msg = getNotificationMessage(requestDTO.getType(), requestDTO.getTripId());

        Notification notification = Notification.builder()
                .setTitle("TTT")
                .setBody(msg)
                .build();

        List<Message> messages = fcmList.stream()
                .map(fcm -> Message.builder()
                        .setNotification(notification)
                        .setToken(fcm.getToken())
                        .build())
                .toList();

        sendFcmNotificationAsync(messages);
    }

    private void sendFcmNotificationAsync(List<Message> messages) {
        ApiFuture<BatchResponse> futureResponse = FirebaseMessaging.getInstance().sendEachAsync(messages);
        ApiFutures.addCallback(futureResponse, new ApiFutureCallback<BatchResponse>() {
            @Override
            public void onSuccess(BatchResponse response) {
                log.info("Successfully sent messages: {}",response.getSuccessCount());
                response.getResponses().forEach(r -> {
                    if (r.isSuccessful()) {
                        log.info("Message sent successfully: {}",r.getMessageId());
                    } else {
                        log.info("Error sending message: {}",r.getException().getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("Error sending messages: {}",t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    private String getNotificationMessage(AlarmType type, Integer tripId){
        switch (type) {
            case GOAL_ACHIEVED:
                return tripId + " 목표를 달성했어요";
            case GOAL_FAILED:
                return tripId + " 목표 기간이 만료됐어요";
            case EXCHANGE_AFTER_GOAL:
                return tripId + " 목표 기간이 만료됐어요, 환전 할까요?";
            case EXCHANGE_AFTER_WEEK:
                return tripId + " 목표 기간이 만료된 지 일주일이 지났어요, 환전 할까요?";
            case LOWEST_EXCHANGE_RATE:
                return "최근 일주일 중 환율이 가장 낮아요";
//                return (rate != null) ? "오늘 환율은 " + rate + " 이에요. 최근 일주일 중 가장 낮아요." : "최근 일주일 중 환율이 가장 낮아요";
            case SELL_THREE_DAYS_LATER:
                return tripId + "를 매도한 지 3일이 지났어요, 환전 할까요?";
            default:
                return "새로운 알림이 도착했습니다.";
        }
    }

}
