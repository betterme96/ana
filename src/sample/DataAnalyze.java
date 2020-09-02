package sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataAnalyze {
    public static void analyzeData(String srcFile, String destFile, String computeFile, int count, int start, int end) throws IOException {
        //数据文件输入流
        FileInputStream dataIn = new FileInputStream(new File(srcFile));

        //数据解析结果文件
        FileOutputStream resultFile = createFileOutputStream(destFile);
        resultFile.write("pulse adc1 adc2\n".getBytes());

        //ADC数据计算文件
        int fileNo = 1;
        FileOutputStream adcFile = createFileOutputStream(computeFile + "_" + fileNo + ".txt");
        adcFile.write("pulse adc1 adc2 absorption\n".getBytes());

        byte[] data = new byte[4];
        int pulse = 0;
        int rotation = 0;
        int preRotation = 0;
        int channel = 0;
        int adc = 0;
        List<Integer> adcList1 = new ArrayList<>();
        List<Integer> adcList2 = new ArrayList<>();
        while (dataIn.read(data) != -1){
            StringBuilder sb = new StringBuilder();
            //flag
            if(data[0] == (byte)0xff){
                if(adcList1.size() > 0 && adcList2.size() > 0){
                    writeCompute2File(pulse, adcList1, adcList2, adcFile, start, end, count);
                }
                pulse = getPulse(data);
                //sb.append("pulse number:" + pulse);
                //sb.append("\n");
            }else{
                rotation = ((data[0] >> 4) & 0xf) - 13;//E-正转，F-反转

                if(preRotation != 0 && preRotation != rotation){
                    //System.out.println("pre:" + preRotation + " cur:" + rotation + "  cur pulse:" + pulse);
                    if(adcList1.size() > 0 && adcList2.size() > 0){
                        writeCompute2File(pulse, adcList1, adcList2, adcFile, start, end, count);
                    }
                    adcFile.close();
                    fileNo++;
                    adcFile = createFileOutputStream(computeFile + "_" + fileNo + ".txt");
                    adcFile.write("pulse adc1 adc2 absorption\n".getBytes());
                }

                preRotation = rotation;

                channel = data[0] & 0xf;
                adc = getADC(data);


                if(channel == 1){
                    adcList1.add(adc);
                    sb.append(pulse + " " + adc + " ");
                }else{
                    adcList2.add(adc);
                    sb.append(adc + "\n");
                }
            }

            resultFile.write(sb.toString().getBytes());
        }
        //System.out.println(adcList1.size() + "  " + adcList2.size());
        writeCompute2File(pulse, adcList1, adcList2, adcFile, start, end, count);
        //seq = write2File(seq, adcList1, adcList2, adcFile, start, end, count);
        dataIn.close();
        resultFile.close();
        adcFile.close();
    }

    /*
    private static int write2File(int sum, List<Integer> adcList1, List<Integer> adcList2, FileOutputStream adcFile, int start, int end, int count) throws IOException {
        StringBuilder temp = new StringBuilder();
        for(int i = start; i < adcList1.size() - end; ++i){
            temp.append(sum + " " + adcList1.get(i));
            sum++;
            temp.append("\n");
        }
        adcFile.write(temp.toString().getBytes());
        adcList1.clear();
        adcList2.clear();
        return sum;
    }

     */

    private static void writeCompute2File(int pulse, List<Integer> adcList1, List<Integer> adcList2, FileOutputStream adcFile, int start, int end, int count) throws IOException {
        System.out.println("pulse -------" + pulse);
        StringBuilder adcAvg = new StringBuilder();
        double adcAvg1 = getAvg(adcList1, start, end, count);
        double adcAvg2 = getAvg(adcList2, start, end, count);
        adcAvg.append(pulse + " " + adcAvg1 + " " + adcAvg2 + " " + Math.log(adcAvg1/adcAvg2));
        //adcAvg.append(pulse + " " + adcAvg1);
        adcAvg.append("\n");
        adcFile.write(adcAvg.toString().getBytes());
        adcList1.clear();
        adcList2.clear();

        /*
        StringBuilder temp = new StringBuilder();
        for(int i = start; i < adcList1.size() - end; ++i){
            temp.append(pulse +" " + adcList1.get(i));
        }
        adcFile.write(temp.toString().getBytes());
        adcList1.clear();
        adcList2.clear();

         */
    }

    private static double getAvg(List<Integer> adc1, int start, int end, int count) {
        System.out.println("size:" + adc1.size());
        int len = (adc1.size() - start - end) * count;
        int sum = 0;
        for(int i = start; i < adc1.size()-end; ++i){
            sum += adc1.get(i);
        }
        return (double)(sum/len);
    }

    private static FileOutputStream createFileOutputStream(String fileName) throws IOException {
        File file = new File(fileName);
        if(!file.exists()){
            System.out.println(fileName + " not exist");
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }


    private static byte[] getChannelData(int i, int dataBase) {
        Random random = new Random();
        int adc = dataBase + random.nextInt() % 5+1;
        byte[] data = new byte[4];
        if(i == 1){
            data[0] = (byte) 0xe1;
        }else{
            data[0] = (byte) 0xe2;
        }
        data[1] = 0x00;
        data[2] = (byte) ((adc >> 8) & 0xff);
        data[3] = (byte) (adc & 0xff);
        return data;
    }

    private static int getADC(byte[] data){
        int res = 0;
        res |= (data[1] & 0xff) << 16;
        res |= (data[2] & 0xff) << 8;
        res |= data[3] & 0xff;
        return res;
    }

    private static int getPulse(byte[] flag) {
        int res = flag[1] << 16 & 0xffffff;
        res |= flag[2] << 8 & 0xffff;
        res |= flag[3] & 0xff;
        return res;
    }

}
