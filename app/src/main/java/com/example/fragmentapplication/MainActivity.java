package com.example.fragmentapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 蓝牙页面相关变量
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String  DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter;
    private ChatService mChatService = null;

//    生产5个Fragment控制器，用于控制5个页面
    private Fragment mTab01 = new WeiXinFragment();
    private Fragment mTab02 = new FrdFragment();
    private Fragment mTab03 = new ContactFragment();
    private Fragment mTab04 = new SettingsFragment();
    private Fragment addCtn = new AddCtnFragment();

    // 定义数据库访问对象
    private MyDAO myDAO;
    private List<Map<String,Object>> listData;
    private Map<String,Object> listItem;

    // 定义字段
    private EditText et_Name = null;
    private EditText et_Tel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initView();
        initFragment();
        initEvent();
        setFragment(0);
        initList_Frd();
        initData_Frd();
        initDAO();
        initList_Ctn();
        initBlueToothContact();
    }

    private LinearLayout mTabWeiXin;
    private LinearLayout mTabFrd;
    private LinearLayout mTabAddress;
    private LinearLayout mTabSettings;
    private LinearLayout mBtnAdd;

    private ImageButton mImgWeixin;
    private ImageButton mImgFrd;
    private ImageButton mImgAddress;
    private ImageButton mImgSettings;

    private void initDAO(){
        myDAO = new MyDAO(this);
        if(myDAO.getRecordsNumber()==0){
            myDAO.insertInfo("tian", "13012345678");
            myDAO.insertInfo("wang","13076543210");
            myDAO.insertInfo("zhang","13471234567");
            myDAO.insertInfo("li","15971234567");
            myDAO.insertInfo("ming","13012345678");
            myDAO.insertInfo("liu","13012345678");
            myDAO.insertInfo("wu","13012345678");
            myDAO.insertInfo("han","13012345678");
        }
    }

//    表示Fragment进行通讯的控制器
    private FragmentManager fm;

    // 用于创建RecView的相关数据
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<String> mList = new ArrayList<>();

    private List<GroupDataBean> mDataList = new ArrayList<>();

    private void initView(){
        mTabWeiXin = (LinearLayout) findViewById(R.id.id_tab_weixin);
        mTabFrd = (LinearLayout) findViewById(R.id.id_tab_frd);
        mTabAddress = (LinearLayout) findViewById(R.id.id_tab_contact);
        mTabSettings = (LinearLayout) findViewById(R.id.id_tab_settings);
        mBtnAdd = (LinearLayout)findViewById(R.id.tab_btn_add) ;

        mImgWeixin = (ImageButton) findViewById(R.id.id_tab_weixin_img);
        mImgFrd = (ImageButton) findViewById(R.id.id_tab_frd_img);
        mImgAddress = (ImageButton) findViewById(R.id.id_tab_contact_img);
        mImgSettings = (ImageButton) findViewById(R.id.id_tab_settings_img);
    }

    private void initRecView_Frd(){
        adapter adapter = new adapter(this);

        RecyclerView recView = findViewById(R.id.recycleview_Frd);

        recView.setLayoutManager(new LinearLayoutManager(this));
        recView.setHasFixedSize(true);
        recView.setAdapter(adapter);

        adapter.setGroupDataList(mDataList);
    }

    private void initRecView_Ctn(){
        VerticalAdapter adapter = new VerticalAdapter(this);

        RecyclerView rcvVertical  = findViewById(R.id.recycleview_Ctn);

        LinearLayoutManager managerVertical = new LinearLayoutManager(this);
        managerVertical.setOrientation(LinearLayoutManager.VERTICAL);

        rcvVertical.setLayoutManager(managerVertical);
        rcvVertical.setHasFixedSize(true);
        rcvVertical.setAdapter(adapter);

        adapter.setVerticalDataList(listData);
    }

    private void initFragment(){
        // getFragmentManager() 版本问题
        fm = getSupportFragmentManager();
        // 控制器实例
        FragmentTransaction transaction = fm.beginTransaction();
        // R.id.id_content -> 选定主布局中的FrameLayout
        transaction.add(R.id.id_content, mTab01);
        transaction.add(R.id.id_content, mTab02);
        transaction.add(R.id.id_content, mTab03);
        transaction.add(R.id.id_content, mTab04);
        transaction.add(R.id.id_content, addCtn);
        transaction.commit();
    }

    private void initEvent(){
        mTabWeiXin.setOnClickListener(this);
        mTabFrd.setOnClickListener(this);
        mTabAddress.setOnClickListener(this);
        mTabSettings.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
    }

    private void initBlueToothContact(){
        // 得到本地蓝牙适配器
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        // 若当前设备不支持蓝牙功能
        if(mBluetoothAdapter == null){
            Toast.makeText(this,"蓝牙不可用",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void BlueToothReaction(){
        if(!mBluetoothAdapter.isEnabled()){
            // 若当前设备蓝牙功能未开启，则开启蓝牙
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        } else{
            if(mChatService==null)
                setupChat();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.scan:
                Intent serverIntent=new Intent(this,DeviceList.class);
                startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
            case R.id.BtOpen:
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                }
                return true;
            case R.id.BtOff:
                mBluetoothAdapter.disable();
                return true;
        }
        return false;
    }

    private void ensureDiscoverable(){
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message){
        if(mChatService.getState() != ChatService.STATE_CONNECTED){
            Toast.makeText(this,R.string.not_connected,Toast.LENGTH_SHORT).show();
            return;
        }

        if(message.length() > 0){
            byte[] send=message.getBytes();
            mChatService.write(send);

            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    private void setupChat(){
        mConversationArrayAdapter=new ArrayAdapter<String>(mTab01.getActivity(), R.layout.list_item);
        mConversationView=(ListView)findViewById(R.id.list_conversation);
        mConversationView.setAdapter(mConversationArrayAdapter);
        mOutEditText=(EditText)findViewById(R.id.edit_text_out);
        mSendButton = (Button)findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                sendMessage(message);
            }
        });
        mChatService = new ChatService(this,mHandler);
        mOutStringBuffer=new StringBuffer("");
    }

    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case ChatService.STATE_CONNECTED:
                            mConversationArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            break;
                    }break;
                case MESSAGE_WRITE:
                    byte[]writeBuf =(byte[])msg.obj;
                    String writeMessage=new String(writeBuf);
                    mConversationArrayAdapter.add("我： " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[]readBuf =(byte[])msg.obj;
                    String readMessage=new String(readBuf,0,msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName+": "
                            +readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName=msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "链接到"+mConnectedDeviceName,Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST),Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };
    public void onActivityResult(int requesstCode, int resultCode, Intent data){
        switch (requesstCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode==Activity.RESULT_OK){
                    String address=data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device=mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    setupChat();
                }else {
                    Toast.makeText(this, "bt_not_enable_leaving",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void addCtn(){
        et_Name = (EditText)this.findViewById(R.id.etxt_name);
        et_Tel = (EditText)this.findViewById(R.id.etxt_tel);
        String p1 = et_Name.getText().toString();
        String p2 = et_Tel.getText().toString();
        Log.d("p1 = ",p1);
        Log.d("p2 = ",p2);
        if(p1.equals("")||p2.equals("")){  //要求输入了信息
            Toast.makeText(getApplicationContext(),"姓名和电话都不能空！",Toast.LENGTH_SHORT).show();
        }else{
            myDAO.insertInfo(p1, p2);  //第2参数转型
            Toast.makeText(getApplicationContext(),"添加成功！",Toast.LENGTH_SHORT).show();
        }
    }

    // 初始化mList列表
    private void initList_Frd() {
        mList.add("TES|top：Moyu");
        mList.add("TES|top：369");
        mList.add("TES|jug：AKi");
        mList.add("TES|jug：Karsa");
        mList.add("TES|mid：KnIGht");
        mList.add("TES|ADC：Photic");
        mList.add("TES|ADC：JakeyLove");
        mList.add("TES|sup：QiuQiu");
        mList.add("TES|sup：Yuyanjia");
        mList.add("SN|top：Biubiu");
        mList.add("SN|top：Bin");
        mList.add("SN|jug：sofM");
        mList.add("SN|mid：Angel");
        mList.add("SN|ADC：Weiwei");
        mList.add("SN|sup：SwordArt");
        mList.add("RW|top：Holder");
        mList.add("RW|top：Crazy");
        mList.add("RW|jug：Haro");
        mList.add("RW|jug：WeiYan");
        mList.add("RW|mid：WuMing");
        mList.add("RW|mid：Ruby");
        mList.add("RW|ADC：ZWuJi");
        mList.add("RW|sup：Ley");
        mList.add("RNG|top：Yuekai");
        mList.add("RNG|top：Langx");
        mList.add("RNG|jug：XLB");
        mList.add("RNG|jug：S1xu");
        mList.add("RNG|mid：Xiaohu");
        mList.add("RNG|ADC：Betty");
        mList.add("RNG|ADC：UZI");
        mList.add("RNG|sup：Ming");
        mList.add("OMG|top：Curse");
        mList.add("OMG|jug：H4cker");
        mList.add("OMG|mid：icon");
        mList.add("OMG|ADC：Kane");
        mList.add("OMG|ADC：SMLZ");
        mList.add("OMG|sup：COLD");
        mList.add("LNG|top：Flandre");
        mList.add("LNG|jug：Xx");
        mList.add("LNG|mid：Maple");
        mList.add("LNG|ADC：Light");
        mList.add("LNG|ADC：Asure");
        mList.add("LNG|sup：Iwendy");
        mList.add("LNG|sup：Duan");
        mList.add("LGD|top：Lies");
        mList.add("LGD|top：Cult");
        mList.add("LGD|jug：Bademan");
        mList.add("LGD|jug：Peanut");
        mList.add("LGD|中路：Fenfen");
        mList.add("LGD|mid：Yuuki");
        mList.add("LGD|ADC：Kramer");
        mList.add("LGD|sup：Killua");
        mList.add("LGD|sup：Chance");
        mList.add("JDG|top：705");
        mList.add("JDG|top：Zoom");
        mList.add("JDG|jug：Kanavi");
        mList.add("JDG|mid：Yagao");
        mList.add("JDG|ADC：LokeN");
        mList.add("JDG|sup：Peace");
        mList.add("JDG|sup：LvMao");
        mList.add("IG|top：The shy");
        mList.add("IG|jug：Ning");
        mList.add("IG|jug：Leyan");
        mList.add("IG|mid：Rookie");
        mList.add("IG|ADC：PUFF");
        mList.add("IG|sup：Baolan");
        mList.add("IG|sup：Fata");
        mList.add("IG|sup：Southwind");
        mList.add("FPX|top：GimGoon");
        mList.add("FPX|top：Khan");
        mList.add("FPX|jug：Tian");
        mList.add("FPX|mid：Doinb");
        mList.add("FPX|ADC：LWX");
        mList.add("FPX|sup：Crisp");
        mList.add("ES|top：xiaobai");
        mList.add("ES|top：CJJ");
        mList.add("ES|jug：Wei");
        mList.add("ES|ADC：Wink");
        mList.add("ES|ADC：Rat");
        mList.add("ES|sup：Alu");
        mList.add("ES|sup：ShiauC");
        mList.add("EDG|top：Jinoo");
        mList.add("EDG|top：Aodi");
        mList.add("EDG|jug：xinyi");
        mList.add("EDG|jug：Junjia");
        mList.add("EDG|mid：Scout");
        mList.add("EDG|ADC：Hope");
        mList.add("EDG|sup：Meiko");
        mList.add("DMO|top：Natural");
        mList.add("DMO|jug：XiaoPeng");
        mList.add("DMO|jug：Bless");
        mList.add("DMO|mid：Xiaowei");
        mList.add("DMO|mid：Xiye");
        mList.add("DMO|ADC：GALA");
        mList.add("DMO|sup：Mark");
        mList.add("BLG|top：ADD");
        mList.add("BLG|top：Kingen");
        mList.add("BLG|jug：Moonlight");
        mList.add("BLG|jug：Meteor");
        mList.add("BLG|mid：FoFo");
        mList.add("BLG|ADC：Wings");
        mList.add("BLG|ADC：Jinjiao");
        mList.add("BLG|sup：Xinmo");
        mList.add("WE|top：Poss");
        mList.add("WE|jug：beishang");
        mList.add("WE|mid：Yimeng");
        mList.add("WE|mid：Plex");
        mList.add("WE|mid：Teacherma");
        mList.add("WE|ADC：Jiumeng");
        mList.add("WE|sup：Missing");
        mList.add("VG|top：Cube");
        mList.add("VG|top：Zdz");
        mList.add("VG|jug：Aix");
        mList.add("VG|jug：Chieftain");
        mList.add("VG|mid：Zeka");
        mList.add("VG|mid：Forge");
        mList.add("VG|ADC：iBoy");
        mList.add("VG|sup：Maestro");
        mList.add("V5|top：997");
        mList.add("V5|top：Alize");
        mList.add("V5|top：clx");
        mList.add("V5|jug：xioahan");
        mList.add("V5|mid：mole");
        mList.add("V5|ADC：Y4");
        mList.add("V5|sup：max");
    }

    // 初始化RecView列表
    private void initData_Frd() {
        for (int i = 0; i < mList.size(); i++) {
            GroupDataBean bean = new GroupDataBean();

            String s = mList.get(i);
            // team
            String team = s.substring(0, s.indexOf("|"));
            // player
            String player = s.substring(s.indexOf("|") + 1, s.length());

            bean.setTeam(team);
            bean.setPlayer(player);

            mDataList.add(bean);
        }

        Log.d(TAG, "initData: " + mDataList.size());
    }

    private void initList_Ctn(){
        listData = new ArrayList<Map<String,Object>>();
        Cursor cursor = myDAO.allQuery();
        while(cursor.moveToNext()){
            String name = cursor.getString(1);
            String tel = cursor.getString(2);
            listItem = new HashMap<String,Object>();
            listItem.put("name", name);
            listItem.put("tel", tel);
            listData.add(listItem);
        }
    }

    private void setFragment(int i){
        FragmentTransaction transaction = fm.beginTransaction();
        hideFragment(transaction);
        // 把图片设置为亮
        // 设置内容区域
        switch (i) {
            case 0:
                Log.d("setSelect1","1");
                transaction.show(mTab01);
                mImgWeixin.setImageResource(R.drawable.tab_weixin_pressed);
                initBlueToothContact();
                BlueToothReaction();
                break;
            case 1:
                Log.d("setSelect2","2");
                transaction.show(mTab02);
                mImgFrd.setImageResource(R.drawable.tab_find_frd_pressed);
                initRecView_Frd();
                break;
            case 2:
                Log.d("setSelect3","3");
                initList_Ctn();
                initRecView_Ctn();
                transaction.show(mTab03);
                mImgAddress.setImageResource(R.drawable.tab_address_pressed);
                break;
            case 3:
                Log.d("setSelect4","4");
                transaction.show(mTab04);
                mImgSettings.setImageResource(R.drawable.tab_settings_pressed);
                break;
            case 4:
                Log.d("setSelect5","5");
                transaction.show(addCtn);
                break;
            default:
                break;
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction){
        transaction.hide(mTab01);
        transaction.hide(mTab02);
        transaction.hide(mTab03);
        transaction.hide(mTab04);
        transaction.hide(addCtn);
    }

    private void resetImg(){
        mImgWeixin.setImageResource(R.drawable.tab_weixin_normal);
        mImgFrd.setImageResource(R.drawable.tab_find_frd_normal);
        mImgAddress.setImageResource(R.drawable.tab_address_normal);
        mImgSettings.setImageResource(R.drawable.tab_settings_normal);
    }

    @Override
    public void onClick(View v) {
        Log.d("ClickId",String.valueOf(v.getId()));
        resetImg();
        switch (v.getId()){
            case R.id.id_tab_weixin:
                Log.d("onClick_WeiXin","0");
                setFragment(0);
                break;
            case R.id.id_tab_frd:
                Log.d("onClick_Frd","1");
                setFragment(1);
                break;
            case R.id.id_tab_contact:
                Log.d("onClick_contact","2");
                setFragment(2);
                break;
            case R.id.id_tab_settings:
                Log.d("onClick_settings","3");
                setFragment(3);
                break;
            case R.id.btn_add:
                Log.d("onClick_AddCtn","4");
                setFragment(4);
                break;
            case R.id.bt_add:
                Log.d("onClick_Btn_Add","5");
                addCtn();
                setFragment(2);
                break;
            default:
                break;
        }
    }
}
