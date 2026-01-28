package com.example.moduleapi.validator;

import com.example.modulecommon.customException.InvalidJoinPolicyException;
import com.example.modulecommon.customException.InvalidPasswordPolicyException;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleuser.model.dto.member.in.JoinDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class MemberRequestValidator {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,15}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*[!@#$%^&*+=-])(?=.*[0-9]).{8,16}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^01(?:0|1|6|9)([0-9]{3,4})([0-9]{4})$");
    private static final Pattern BIRTH_PATTERN = Pattern.compile("^\\d{4}/\\d{1,2}/\\d{1,2}$");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,20}$");

    public void validateUserIdAndPassword(String userId, String password){
        validateUserId(userId);
        validatePassword(password);
    }

    public void validateUserId(String userId) {
        if(userId == null || !USER_ID_PATTERN.matcher(userId).matches())
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "userId Invalid");
    }

    public void validatePassword(String password) {
        if(password == null || !PASSWORD_PATTERN.matcher(password).matches())
            throw new InvalidPasswordPolicyException(ErrorCode.BAD_REQUEST, "password Invalid");
    }

    public void validateJoinDTO(JoinDTO joinDTO) {
        log.info("MemberRequestValidator.validateJoinDTO");
        validateUserId(joinDTO.userId());
        validatePassword(joinDTO.userPw());
        validateUserName(joinDTO.userName());
        validateNickname(joinDTO.nickname());
        validatePhone(joinDTO.phone());
        validateBirth(joinDTO.birth());
        validateEmail(joinDTO.userEmail());
    }

    public void validateEmail(String email) {
        if(email == null || !EMAIL_PATTERN.matcher(email).matches())
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "email Invalid");
    }

    public void validatePhone(String phone) {
        if(phone == null || !PHONE_PATTERN.matcher(phone).matches())
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "phone Invalid");
    }

    public void validateUserName(String userName) {
        if(userName == null || userName.length() < 2)
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "userName Invalid");
    }

    public void validateBirth(String birth) {
        if(birth == null || !BIRTH_PATTERN.matcher(birth).matches())
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "birth Invalid");
    }

    public void validateNickname(String nickname) {
        if(nickname != null && !NICKNAME_PATTERN.matcher(nickname).matches())
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "nickname Invalid");
    }

    public void validateSearchId(String userName, String userPhone, String userEmail) {
        log.info("validate SearchId");
        validateUserName(userName);

        if(userPhone == null && userEmail == null){
            log.warn("MemberRequestValidator.validateSearchId :: phone and email is null");
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "phone and email is null");
        }

        if(userPhone != null && userEmail != null){
            log.warn("MemberRequestValidator.validateSearchId :: phone and email is not null");
            throw new InvalidJoinPolicyException(ErrorCode.BAD_REQUEST, "phone and email is not null");
        }


        if(userPhone != null)
            validatePhone(userPhone);

        if(userEmail != null)
            validateEmail(userEmail);
    }

    public void validateSearchPassword(String userId, String userName, String userEmail) {
        log.info("MemberRequestValidator.validateSearchPassword");

        validateUserId(userId);
        validateUserName(userName);
        validateEmail(userEmail);
    }
}
