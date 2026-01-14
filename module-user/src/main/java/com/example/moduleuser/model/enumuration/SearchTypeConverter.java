package com.example.moduleuser.model.enumuration;


import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
public class SearchTypeConverter implements Converter<String, AdminMemberSearchType> {

    @Override
    public AdminMemberSearchType convert(String s) {
        System.out.println("im converter : " + s);

        if(s == null || s.isBlank()){
            System.out.println("searchType is null");
            return null;
        }


//        return AdminMemberSearchType.from(s);


        AdminMemberSearchType result = AdminMemberSearchType.from(s);

        return result;
    }
}
