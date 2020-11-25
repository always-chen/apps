/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.scankitdemo;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.hms.framework.common.StringUtils;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener {

    /**
     * Define requestCode.
     */
    public static final int CAMERA_REQ_CODE = 111;
    public static final int DEFINED_CODE = 222;
    public static final int BITMAP_CODE = 333;
    public static final int MULTIPROCESSOR_SYN_CODE = 444;
    public static final int MULTIPROCESSOR_ASYN_CODE = 555;
    public static final int GENERATE_CODE = 666;
    public static final int DECODE = 1;
    public static final int GENERATE = 2;
    private static final int REQUEST_CODE_SCAN_ONE = 0X01;
    private static final int REQUEST_CODE_DEFINE = 0X0111;
    private static final int REQUEST_CODE_SCAN_MULTI = 0X011;
    public static final String DECODE_MODE = "decode_mode";
    public static final String RESULT = "SCAN_RESULT";
    private TextView tv_result;
    private SharedPreferences msp; // 存储
    private CheckBox chb_in_workday,
            chb_in_check,
            chb_in_no_repeat_date,
            chb_in_no_repeat_code;
    private CheckBox chb_out_date,
            chb_out_price,
            chb_out_code,
            chb_out_num,
            chb_out_valid_code;
    private RecyclerView rv;
    private List<ElecCheckModel> list;
    private Button clearBtn;
    private boolean is_scan_daily, is_scan_valid, is_scan_norepeat_date, is_scan_norepeat_num,
            is_dis_date, is_dis_price, is_dis_code, is_dis_num, is_dis_valid;

    private ArrayList<ArrayList<String>> recordList;
    private static String[] title = { "序号","代码","号码","日期","价格","校验码","校验码后6位" };
    private File file;
    private String fileName;

    private List<String> holidayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mwcmain);
        InputStream is = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            is = getAssets().open("holiday");
            reader = new InputStreamReader(is);
            bufferedReader = new BufferedReader(reader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                holidayList.add(str);
            }
            System.out.println("假期" + holidayList.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("获取假期失败");
        } finally {
            try {
                if(bufferedReader != null){
                    bufferedReader.close();
                }
                if(reader != null) {
                    reader.close();
                }
                if(is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tv_result = (TextView)findViewById(R.id.tv_result);
        clearBtn = (Button)findViewById(R.id.btn_clear);
        chb_in_workday = (CheckBox)findViewById(R.id.chb_in_workday);
        chb_in_check = (CheckBox)findViewById(R.id.chb_in_check);
        chb_in_no_repeat_date = (CheckBox)findViewById(R.id.chb_in_no_repeat_date);
        chb_in_no_repeat_code = (CheckBox)findViewById(R.id.chb_in_no_repeat_code);
        chb_in_workday.setOnCheckedChangeListener(this);
        chb_in_check.setOnCheckedChangeListener(this);
        chb_in_no_repeat_date.setOnCheckedChangeListener(this);
        chb_in_no_repeat_code.setOnCheckedChangeListener(this);

        is_scan_daily = true;
        is_scan_valid = true;
        is_scan_norepeat_date = true;
        is_scan_norepeat_num = true;
        is_dis_date = true;
        is_dis_price = true;
        is_dis_code = true;
        is_dis_num = true;
        is_dis_valid = true;

        chb_out_date = (CheckBox)findViewById(R.id.chb_out_date);
        chb_out_price = (CheckBox)findViewById(R.id.chb_out_price);
        chb_out_code = (CheckBox)findViewById(R.id.chb_out_code);
        chb_out_num = (CheckBox)findViewById(R.id.chb_out_num);
        chb_out_valid_code = (CheckBox)findViewById(R.id.chb_out_valid_code);
        chb_out_date.setOnCheckedChangeListener(this);
        chb_out_price.setOnCheckedChangeListener(this);
        chb_out_code.setOnCheckedChangeListener(this);
        chb_out_num.setOnCheckedChangeListener(this);
        chb_out_valid_code.setOnCheckedChangeListener(this);
        list = new ArrayList<>();
        rv = findViewById(R.id.item_list);
        assert rv != null;
        msp = super.getSharedPreferences("list", MODE_PRIVATE);
//        writeInfo("01,10,051001900411,71830740,267.25,20200907,47482452873556107303,3340,");
//        writeInfo("01,10,051001900411,71830740,267.25,20200907,47482452873556107303,3340,");
        readInfo(); // 读取存储的数据
        rv.setAdapter(new MyRvAdapter(list));
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                    .setTitle("清除确认")
                    .setMessage("确认清除当前所有数据吗")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            writeInfo(null);
                            tv_result.setText("[]");
                        }
                    })
                    .setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println("弹窗点击否");
                        }
                    }).show();
            }
        });
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //Set noTitleBar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }


    }
    public class MyRvAdapter extends RecyclerView.Adapter<MyRvAdapter.MyTvHolder> {
        private List<ElecCheckModel> modelList;

        public MyRvAdapter(List<ElecCheckModel> modelList) {
            this.modelList = modelList;
        }

        @NonNull
        @Override
        public MyRvAdapter.MyTvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyRvAdapter.MyTvHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyRvAdapter.MyTvHolder holder, int position) {
            System.out.println(2222);
            try {
                holder.tv_index.setText("" + (position + 1));
                ElecCheckModel tModel = modelList.get(position);
                System.out.println(tModel);
                holder.tv_code.setText(tModel.getCode());
                holder.tv_num.setText(tModel.getNum());
                holder.tv_date.setText(tModel.getDate());
                holder.tv_price.setText(tModel.getPrice());
                holder.tv_valid_code.setText(tModel.getValidCode());
                holder.tv_last_six_valid_code.setText(tModel.getLastSixValidCode());
                if (is_dis_code) {
                    holder.tv_code.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_code.setVisibility(View.INVISIBLE);
                }
                if (is_dis_num) {
                    holder.tv_num.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_num.setVisibility(View.INVISIBLE);
                }
                if (is_dis_date) {
                    holder.tv_date.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_date.setVisibility(View.INVISIBLE);
                }
                if (is_dis_price) {
                    holder.tv_price.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_price.setVisibility(View.INVISIBLE);
                }
                if (is_dis_valid) {
                    holder.tv_valid_code.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_valid_code.setVisibility(View.INVISIBLE);
                }
            }catch(Exception e) {
                System.out.println(e.toString());
            }
        }

        @Override
        public int getItemCount() {
            return modelList == null ? 0 : modelList.size();
        }
        class MyTvHolder extends RecyclerView.ViewHolder {
            TextView tv_index, tv_code, tv_num, tv_price, tv_date, tv_valid_code,
            tv_last_six_valid_code;

            MyTvHolder(View itemView) {
                super(itemView);
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        TextView tv = (TextView) v.findViewById(R.id.tv_index);
                        final int dataIndex = Integer.valueOf((String)tv.getText());
                        System.out.println("长按了子元素" + dataIndex);
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("删除确认")
                                .setMessage("确认删除当前数据吗")
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        System.out.println("弹窗点击是" + which);
                                        list.remove(dataIndex - 1);
                                        rv.setAdapter(new MyRvAdapter(list));
                                    }
                                })
                                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        System.out.println("弹窗点击否");
                                    }
                                }).show();
                        return false;
                    }
                });
                tv_index = (TextView) itemView.findViewById((R.id.tv_index));
                tv_code = (TextView) itemView.findViewById((R.id.tv_code));
                tv_num = (TextView) itemView.findViewById((R.id.tv_num));
                tv_price = (TextView) itemView.findViewById((R.id.tv_price));
                tv_date = (TextView) itemView.findViewById((R.id.tv_date));
                tv_valid_code = (TextView) itemView.findViewById((R.id.tv_valid_code));
                tv_last_six_valid_code = (TextView) itemView.findViewById((R.id.tv_last_six_valid_code));
            }
            public void setVisibility(int visibility) {
                itemView.setVisibility(visibility);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                if (visibility == View.VISIBLE) {
                    params.width = RecyclerView.LayoutParams.MATCH_PARENT;
                    params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
                } else {
                    params.width = 0;
                    params.height = 0;
                }
                itemView.setLayoutParams(params);
            }
        }
    }
    // 01,10,051001900411,71830740,267.25,20200907,47482452873556107303,3340,
    // 2 代码 3 号码 4 价格 5 日期 6 校验码
    public ElecCheckModel getElecCheckModel(String result) {
        boolean isOk = true;
        if(result == null || result.isEmpty()) {
            Toast.makeText(MainActivity.this, "发票信息错误", Toast.LENGTH_SHORT).show();
            return null;
        }
        String[] strArr = result.split(",");
        if (strArr.length < 7 || strArr[2].length() != 12 || strArr[3].length() != 8 || strArr[6].length() != 20) {
            Toast.makeText(MainActivity.this, "非电子发票", Toast.LENGTH_SHORT).show();
            isOk = false;
            return null;
        }
        String code = strArr[2];
        String num = strArr[3];
        String date = strArr[5];
        String price = strArr[4];
        String validCode = strArr[6];
        String lastSixValidCode = strArr[6].substring(strArr[6].length() - 6);
        if (isOk && is_scan_norepeat_date) {
            // 日期是否重复校验
            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).getDate().equals(date)) {
                    Toast.makeText(MainActivity.this, "发票日期与第"+ (i + 1) +"张重复", Toast.LENGTH_SHORT).show();
                    isOk = false;
                    return null;
                }
            }
        }
        if (isOk && is_scan_norepeat_num) {
            // 发票代码，号码是否重复校验
            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).getNum().equals(num) && list.get(i).getCode().equals(code)) {
                    Toast.makeText(MainActivity.this, "发票号码、代码与第"+ (i + 1) +"张重复", Toast.LENGTH_SHORT).show();
                    isOk = false;
                    return null;
                }
            }
        }
        if (isOk && is_scan_daily) {
            // 工作日发票
            if(holidayList.indexOf(date) > -1) {
                Toast.makeText(MainActivity.this, "发票日期不能为节假日", Toast.LENGTH_SHORT).show();
                isOk = false;
                return null;
            }
        }
        if(!isOk){
            return null;
        }
        // String code, String num, String date, String price, String validCode, String lastSixValidCode
        return new ElecCheckModel(code,
                num,
                date,
                price,
                validCode,
                lastSixValidCode);
    }
    public void writeInfo(String info) {
        try {
            SharedPreferences.Editor editor=msp.edit();
            if (null == info) {
                list.clear();
            } else {
                ElecCheckModel elecCheckModel = getElecCheckModel(info);
                if(elecCheckModel == null){
                    return;
                }
                list.add(elecCheckModel);
            }
            rv.setAdapter(new MyRvAdapter(list));
            editor.putString("list",new Gson().toJson(list));
            editor.commit();
        }catch (Exception e){
            System.out.print(e.toString());
        }

    }
    public void readInfo() {
        try {
            String listStr = msp.getString("list", "[]");
            tv_result.setText(listStr);
            // 把JSON格式的字符串转为List
            list = new Gson().fromJson(listStr, new TypeToken<List<ElecCheckModel>>(){}.getType());
        }catch (Exception e){
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            System.out.print(e.toString());
        }
    }
    /**
     * Call the default view.
     */
    public void loadScanKitBtnClick(View view) {
        requestPermission(CAMERA_REQ_CODE, DECODE);
    }

    /**
     * Call the customized view.
     */
    public void newViewBtnClick(View view) {
        requestPermission(DEFINED_CODE, DECODE);
    }

    /**
     * Call the bitmap.
     */
    public void bitmapBtnClick(View view) {
        requestPermission(BITMAP_CODE, DECODE);
    }

    /**
     * Call the MultiProcessor API in synchronous mode.
     */
    public void multiProcessorSynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_SYN_CODE, DECODE);
    }

    /**
     * Call the MultiProcessor API in asynchronous mode.
     */
    public void multiProcessorAsynBtnClick(View view) {
        requestPermission(MULTIPROCESSOR_ASYN_CODE, DECODE);
    }

    /**
     * Start generating the barcode.
     */
    public void generateQRCodeBtnClick(View view) {
        requestPermission(GENERATE_CODE, GENERATE);
    }

    /**
     * Apply for permissions.
     */
    private void requestPermission(int requestCode, int mode) {
        if (mode == DECODE) {
            decodePermission(requestCode);
        } else if (mode == GENERATE) {
            generatePermission(requestCode);
        }
    }

    /**
     * Apply for permissions.
     */
    private void decodePermission(int requestCode) {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                requestCode);
    }

    /**
     * Apply for permissions.
     */
    private void generatePermission(int requestCode) {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                requestCode);
    }

    /**
     * Call back the permission application result. If the permission application is successful, the barcode scanning view will be displayed.
     * @param requestCode Permission application code.
     * @param permissions Permission array.
     * @param grantResults: Permission application result array.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || grantResults == null) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == GENERATE_CODE) {
            Intent intent = new Intent(this, GenerateCodeActivity.class);
            this.startActivity(intent);
        }

        if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Default View Mode
        if (requestCode == CAMERA_REQ_CODE) {
            ScanUtil.startScan(this, REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
        }
        //Customized View Mode
        if (requestCode == DEFINED_CODE) {
            Intent intent = new Intent(this, DefinedActivity.class);
            this.startActivityForResult(intent, REQUEST_CODE_DEFINE);
        }
        //Bitmap Mode
        if (requestCode == BITMAP_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, BITMAP_CODE);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
        }
        //Multiprocessor Synchronous Mode
        if (requestCode == MULTIPROCESSOR_SYN_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, MULTIPROCESSOR_SYN_CODE);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
        }
        //Multiprocessor Asynchronous Mode
        if (requestCode == MULTIPROCESSOR_ASYN_CODE) {
            Intent intent = new Intent(this, CommonActivity.class);
            intent.putExtra(DECODE_MODE, MULTIPROCESSOR_ASYN_CODE);
            this.startActivityForResult(intent, REQUEST_CODE_SCAN_MULTI);
        }
    }

    /**
     * Event for receiving the activity result.
     *
     * @param requestCode Request code.
     * @param resultCode Result code.
     * @param data        Result.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        //Default View
        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Intent intent = new Intent(this, DisPlayActivity.class);
                intent.putExtra(RESULT, obj);
                startActivity(intent);
            }
            //MultiProcessor & Bitmap
        } else if (requestCode == REQUEST_CODE_SCAN_MULTI) {
            Parcelable[] obj = data.getParcelableArrayExtra(CommonActivity.SCAN_RESULT);
                if (obj != null && obj.length > 0) {
                    //Get one result.
//                    if (obj.length == 1) {
//                        if (obj[0] != null && !TextUtils.isEmpty(((HmsScan) obj[0]).getOriginalValue())) {
//                            Intent intent = new Intent(this, DisPlayActivity.class);
//                            intent.putExtra(RESULT, obj[0]);
//                            startActivity(intent);
//                        }
//                    } else {
//                        Intent intent = new Intent(this, DisPlayMulActivity.class);
//                        intent.putExtra(RESULT, obj);
//                        startActivity(intent);
//                    }
                    if (obj.length == 1) {
                        if (obj[0] != null && !TextUtils.isEmpty(((HmsScan) obj[0]).getOriginalValue())) {
                            HmsScan thmsScan = (HmsScan)(obj[0]);
                            tv_result.setText(thmsScan.getShowResult());
                            writeInfo(thmsScan.getShowResult());
//                            list.add(getElecCheckModel(thmsScan.getShowResult()));
                        }
                    }
                }
            //Customized View
        } else if (requestCode == REQUEST_CODE_DEFINE) {
            HmsScan obj = data.getParcelableExtra(DefinedActivity.SCAN_RESULT);
            if (obj != null) {
                Intent intent = new Intent(this, DisPlayActivity.class);
                intent.putExtra(RESULT, obj);
                startActivity(intent);
            }
        }
    }
    public void filterRecycleView(){
        rv.setAdapter(new MyRvAdapter(list));
    }
    /**
     * 导出excel
     * @param view
     */
    public void exportExcel(View view) {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };
        int REQUEST_EXTERNAL_STORAGE = 1;
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        file = new File(getSDPath() + "/Record");
        makeDir(file);
        System.out.println("创建文件夹" + file.exists());
        ExcelUtils.initExcel(file.toString() + "/电子发票.xls", title);
        fileName = getSDPath() + "/Record/电子发票.xls";
        ExcelUtils.writeObjListToExcel(getRecordData(), fileName, this);
    }

    /**
     * 将数据集合 转化成ArrayList<ArrayList<String>>
     * @return
     */
    private  ArrayList<ArrayList<String>> getRecordData() {
        recordList = new ArrayList<>();
        try {
            for (int i = 0; i < list.size(); i++) {
                ElecCheckModel eInvoice = list.get(i);
                ArrayList<String> beanList = new ArrayList<String>();
                beanList.add(i + 1 + "");
                beanList.add(eInvoice.getCode());
                beanList.add(eInvoice.getNum());
                beanList.add(new SimpleDateFormat("yyyy/MM/dd").format(new SimpleDateFormat("yyyyMMdd").parse(eInvoice.getDate())));
                beanList.add(eInvoice.getPrice());
                beanList.add(eInvoice.getValidCode());
                beanList.add(eInvoice.getLastSixValidCode());
                recordList.add(beanList);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return recordList;
    }

    private  String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;
    }

    public  void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }
    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
        switch (checkBox.getId()) {
            case R.id.chb_in_workday:
                // 选中工作日
                if (isChecked) {
                    is_scan_daily = true;
                } else {
                    is_scan_daily = false;
                }
                break;
            case R.id.chb_in_check:
                // 选中校验
                if (isChecked) {
                    is_scan_valid = true;
                    Toast.makeText(MainActivity.this, "暂不支持校验发票信息", Toast.LENGTH_SHORT).show();
                } else {
                    is_scan_valid = false;
                }
                break;
            case R.id.chb_in_no_repeat_code:
                // 选中发票不重复
                if (isChecked) {
                    is_scan_norepeat_num = true;
                } else {
                    is_scan_norepeat_num = false;
                }
                break;
            case R.id.chb_in_no_repeat_date:
                // 选中日期不重复
                if (isChecked) {
                    is_scan_norepeat_date = true;
                } else {
                    is_scan_norepeat_date = false;
                }
                break;
            case R.id.chb_out_code:
                // 选中显示发票代码
                if (isChecked) {
                    is_dis_code = true;
                } else {
                    is_dis_code = false;
                }
                filterRecycleView();
                break;
            case R.id.chb_out_num:
                // 选中显示发票号码
                if (isChecked) {
                    is_dis_num = true;
                } else {
                    is_dis_num = false;
                }
                filterRecycleView();
                break;
            case R.id.chb_out_date:
                // 选中显示发票日期
                if (isChecked) {
                    is_dis_date = true;
                } else {
                    is_dis_date = false;
                }
                filterRecycleView();
                break;
            case R.id.chb_out_valid_code:
                // 选中显示发票校验码
                if (isChecked) {
                    is_dis_valid = true;
                } else {
                    is_dis_valid = false;
                }
                filterRecycleView();
                break;
            case R.id.chb_out_price:
                // 选中显示发票价格
                if (isChecked) {
                    is_dis_price = true;
                } else {
                    is_dis_price = false;
                }
                filterRecycleView();
                break;
            default:
                break;
        }
    }
}
