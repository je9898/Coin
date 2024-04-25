package com.coin.backend.controller;

import com.coin.backend.dto.DtoUser;
import com.coin.backend.service.ServiceQrSocket;
import com.coin.backend.service.ServiceUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ControllerQR {
    @Autowired
    private ServiceUser serviceUser;

    //qr����
    @RequestMapping(value={"/qrCode"}, method={RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Object qrCode() throws WriterException, IOException {
        //qr ������
        int width = 300;
        int height = 300;
        //BitMatrix matrix = new MultiFormatWriter().encode("http://13.209.199.128:8090/qrCheck", BarcodeFormat.QR_CODE, width, height);
        //BitMatrix matrix = new MultiFormatWriter().encode("http://192.168.219.55:8090/qrCheck", BarcodeFormat.QR_CODE, width, height);
        BitMatrix matrix = new MultiFormatWriter().encode("http://localhost:8090/qrCheck", BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(out.toByteArray());
        }
    }

    //qr����� ������� ���
    @RequestMapping(value={"/qrCheck"}, method={RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public void qrCheck() throws Exception {
        //ip����
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = req.getHeader("X-FORWARDED-FOR");
        if (ip == null){
            ip = req.getRemoteAddr();
        }

        //ip�� id ��ȸ����.
        List<DtoUser> list = serviceUser.userId(ip);  //id �����

        //3000�� ������ Socket ������.
        ServiceQrSocket io = new ServiceQrSocket();
        io.send(ip, list.get(0).getId(), list.get(0).getPw());
    }
}
