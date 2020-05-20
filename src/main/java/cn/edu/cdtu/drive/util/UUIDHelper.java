package cn.edu.cdtu.drive.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author HarrisonLee
 * @date 2020/4/11 0:51
 */
public class UUIDHelper {

    public static String rand(Integer length){
        StringBuilder uid = new StringBuilder(length);
        //产生length位的强随机数
        Random rd = new SecureRandom();
        for (int i = 0; i < length; i++) {
            //产生0-2的3位随机数
            int type = rd.nextInt(3);
            switch (type){
                case 0:
                    //0-9的随机数
                    uid.append(rd.nextInt(10));
                    break;
                case 1:
                    //ASCII在65-90之间为大写,获取大写随机
                    uid.append((char)(rd.nextInt(25)+65));
                    break;
                case 2:
                    //ASCII在97-122之间为小写，获取小写随机
                    uid.append((char)(rd.nextInt(25)+97));
                    break;
                default:
                    break;
            }
        }
        return uid.toString();
    }
}
