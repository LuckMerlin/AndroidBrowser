package com.luckmerlin.browser.http;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.http.Answer;
import com.luckmerlin.http.AnswerBody;
import com.luckmerlin.http.Headers;
import com.luckmerlin.http.OnAnswerReceived;
import java.io.InputStream;

public class ChunkResponseParser<T> implements OnAnswerReceived {

    @Override
    public void onAnswerReceived(Answer answer) {
        if (null==answer){
            return;
        }
        Headers headers=answer.getHeaders();
        String encoding=null!=headers?headers.getTransferEncoding():null;
        if (null==encoding||!encoding.equals(Headers.CHUNKED)){
            return;
        }
        AnswerBody answerBody=answer.getAnswerBody();
        InputStream inputStream=null!=answerBody?answerBody.getStream():null;
        if (null==inputStream){
            return;
        }
        try {
            byte[] buffer=new byte[1024];
            int length=0;
            Debug.D("EEEEA  "+inputStream);
            while ((length=inputStream.read(buffer))>=0){
                Debug.D("EEEE  "+new String(buffer,0,length));
            }
        }catch (Exception e){

        }
    }
}
