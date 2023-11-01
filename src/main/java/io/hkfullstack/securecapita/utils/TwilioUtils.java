package io.hkfullstack.securecapita.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwilioUtils {

    public static final String FROM_NUMBER = "+18777152217";
    public static final String SID_KEY = "ACb2f43604c83c00dd00bdcf6b739b33dd";
    public static final String TOKEN_KEY = "";

    public static void sendSMS(String to, String messageBody) {
        Twilio.init(SID_KEY, TOKEN_KEY);
        Message message = Message.creator(new PhoneNumber("+" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        log.info("Message: {} ", message);
    }

}
