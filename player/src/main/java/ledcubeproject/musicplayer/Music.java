package ledcubeproject.musicplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user123.musicplayer.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ledcubeproject.models.ledcube.LedCubeDataModel;
import ledcubeproject.models.ledcubecontroller.BluetoothLedCubeController;
import ledcubeproject.models.musicprocessor.Player;
import ledcubeproject.models.musicprocessor.audiodevice.AndroidAudioDevice;
import ledcubeproject.models.musicprocessor.processor.SimpleSpectrumAnalyzer;
import ledcubeproject.models.musicprocessor.processor.spectrumstrategy.TwentyBands;
import ledcubeproject.models.util.Callback;
import ledcubeproject.ui.ListDialog;

public class Music extends AppCompatActivity {

    //播放play&暫停pause的轉換
    //循環replay&隨機random的轉換
    //收藏
    //清單排序(dialog)-1.依字母  2.有收藏的放前面
    //CD圖片->歌曲有圖片就會變歌曲的圖
    //歌名顯示在時間條上面那
    //FAQ先留著 感覺很酷!

    //   ( ￣□￣)/ <(￣ㄧ￣ ) <(￣ㄧ￣ ) 致敬

    /* 歌曲清單
    | 歌曲名稱                     時間 |
     */

    public static final int FIRST_ITEM = 0;
    public static final int SECOND_ITEM = 1;
    //////////////////////////////////////////////////////////////////
    //file manager  宣告
    private static final String ROOT = "/";
    private static final String PRE_LEVEL = "..";

    AndroidAudioDevice anaudev = new AndroidAudioDevice();
    Player player = new Player(anaudev);
    SimpleSpectrumAnalyzer simpleSpectrumAnalyzer = new SimpleSpectrumAnalyzer();
    LedCubeDataModel ledCubeDataModel = new LedCubeDataModel(6);
    BluetoothLedCubeController ledCubeController = new BluetoothLedCubeController();
    ListDialog pairedListDialog;
    EffectSettingDialog effectSettingDialog;
    ImageView playButton;
    SeekBar durationBar;
    TextView txvTime;
    PlayingThread playing = new PlayingThread();
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what < durationBar.getMax())
                durationBar.setProgress(msg.what);
            else
                durationBar.setProgress(durationBar.getMax());
        }
    };
    ButtonSetListener buttonSetListener = new ButtonSetListener();
    private ImageView previous;
    private ImageView next;
    private ImageView bluetoothButton;
    private ImageView effect;
    private ImageView albumCover;
    private TextView txvFilename;
    private String IMG_ITEM = "image";
    private String NAME_ITEM = "name";
    private List<Map<String, Object>> filesList;
    private List<String> names;
    private List<String> paths;
    private Hashtable<String, Integer> fileTable = new Hashtable<>();
    private int[] fileImg = {
            R.drawable.directory,
            R.drawable.file};
    private SimpleAdapter simpleAdapter;
    private ListView listView;
    private String nowPath;
    private ArrayList<FileListener> fileListenerList = new ArrayList<>();
    // 路徑存放處
    private String mp3Path[] = {"/sdcard/Download", "/sdcard/Ringtones", "/sdcard/Music"};

    LedCubeMotion ledCubeMotion;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        /*findViewById*/
        playButton = (ImageView) findViewById(R.id.play);
        previous = (ImageView) findViewById(R.id.previous);
        next = (ImageView) findViewById(R.id.next);
        effect = (ImageView) findViewById(R.id.effect);
        bluetoothButton = (ImageView) findViewById(R.id.bluetooth);
        albumCover = (ImageView) findViewById(R.id.albumCover);
        //arcMenuAndroid = (ArcMenu) findViewById(R.id.arcmenu_android_example_layout);
        durationBar = (SeekBar) findViewById(R.id.seekBar2);
        txvTime = (TextView) findViewById(R.id.timeline);
        txvFilename = (TextView) findViewById(R.id.filename);

        /*設定各個按鈕動作*/
        playButton.setOnClickListener(buttonSetListener);
        previous.setOnClickListener(buttonSetListener);
        next.setOnClickListener(buttonSetListener);
        bluetoothButton.setOnClickListener(buttonSetListener);
        effect.setOnClickListener(buttonSetListener);


//        arcMenuAndroid.setStateChangeListener(new StateChangeListener() {
//            @Override
//            public void onMenuOpened() {
//                //TODO something when menu is opened
//            }
//
//            @Override
//            public void onMenuClosed() {
//                //TODO something when menu is closed
//            }
//        });

        durationBar.setOnSeekBarChangeListener(new DurationBarListener());

        simpleSpectrumAnalyzer.setSpectrumStrategy(new TwentyBands());

        //durationBar motion
        player.addPlayingAction(new Callback() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(player.getCurrentPosition());
            }
        });

        //LED cube motion
        ledCubeMotion = new LedCubeMotion(player, simpleSpectrumAnalyzer, ledCubeController, ledCubeDataModel);


        pairedListDialog = new ListDialog(this, new PairedListListener(this), "已配對裝置列表");
        effectSettingDialog = new EffectSettingDialog(this, R.layout.effect_dialog, "效果列表", ledCubeMotion);


        //file manager 呼叫
        findMp3();
        initView();
        for(int i = 0; i < mp3Path.length; i++)
        {
            FileListener fileListener = new FileListener(mp3Path[i], FileObserver.CREATE | FileObserver.DELETE);
            fileListenerList.add(fileListener);
            fileListener.startWatching();
        }

        if (ledCubeController.isConnected())
        {
            bluetoothButton.setImageDrawable(this.getResources().getDrawable(R.drawable.bluetooth_blue));
            player.addPlayingAction(ledCubeMotion);
        }

        if(paths.size() > 0)
            initPlayerAndGo(paths.get(0), false);
    }

    public void initPlayerAndGo(String filePath, boolean play) {
        if(filePath == null)
            return;
        nowPath = filePath;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        player.pause();
//        while (player.isPlaying());   //造成當機，原因未知
        long t = System.nanoTime();
        while(System.nanoTime() - t < 1000000000 / 2);
        player.init(filePath);
        durationBar.setMax(player.getDuration());
        Log.d("duration", ""+ (player.getDuration() / 1000));
        durationBar.setProgress(0);
        String fileName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if(fileName == null || fileName.equals(""))
            fileName = nowPath.substring(nowPath.lastIndexOf('/') + 1);
        txvFilename.setText(fileName);
        if (play) {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            playing = new PlayingThread();
            playing.start();
        } else {
            playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
            player.pause();
        }

        byte[] imageData = mmr.getEmbeddedPicture();
        if(imageData != null)
        {
            Bitmap img = BitmapFactory.decodeStream(new ByteArrayInputStream(imageData));
             albumCover.setImageDrawable(new BitmapDrawable(getResources(), img));
        }
        else
            albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cd));
//        try {
//            Mp3File song = new Mp3File(nowPath);
//            if (song.hasId3v2Tag()){
//                ID3v2 id3v2tag = song.getId3v2Tag();
//                byte[] imageData = id3v2tag.getAlbumImage();
//                //converting the bytes to an image
//                if(imageData != null) {
//                    Bitmap img = BitmapFactory.decodeStream(new ByteArrayInputStream(imageData));
//                    albumCover.setImageDrawable(new BitmapDrawable(getResources(), img));
//                }
//                else
//                    albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cd));
//            }
//            else
//                albumCover.setImageDrawable(getResources().getDrawable(R.drawable.cd));
//        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
//            e.printStackTrace();
//        }


    }

    //file manager 函式
    private void initView() {
        //設定simpleAdapter
        simpleAdapter = new SimpleAdapter(this,
                filesList, R.layout.simple_adapter, new String[]{IMG_ITEM, NAME_ITEM},
                new int[]{R.id.image, R.id.text});
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(simpleAdapter);
        //對於點選，所做的後續操作
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String target = paths.get(position);//取得是按下哪一個檔案或資料夾的路徑(String)

                File file = new File(target);
                if (file.canRead()) {//如果可以讀取
                    if (file.isDirectory()) {//如果是資料夾
                        nowPath = paths.get(position);//取得路徑
                        getFileDirectory(paths.get(position));//設定
                        simpleAdapter.notifyDataSetChanged();//顯示
                        //Toast.makeText(MainActivity.this, nowPath, Toast.LENGTH_LONG).show();
                    } else {//如果是檔案
                        //Toast.makeText(Music.this, R.string.is_not_directory, Toast.LENGTH_SHORT).show();
                        nowPath = paths.get(position);//路徑位址!!!!!!<-----------------
                        initPlayerAndGo(nowPath, true);
                        //Toast.makeText(Music.this, nowPath, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Toast.makeText(Music.this, R.string.can_not_read, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findMp3() {
        //初始化

            filesList = new ArrayList<>();
            names = new ArrayList<>();
            paths = new ArrayList<>();

        //filesList.clear();
        //paths.clear();

        //取得路徑裡面的檔案
        for(String str : mp3Path)
            getFileDirectory(str);
        /*if(simpleAdapter != null)
            simpleAdapter.notifyDataSetChanged();*/
    }

    private void getFileDirectory(String path) {
        //filesList.clear();
        //paths.clear();

        File[] files = new File(path).listFiles();
        for (int i = 0; i < files.length; i++) {
            System.out.println(i + ": " + files[i].getPath());
            if(files[i].isDirectory())
                getFileDirectory(files[i].getPath());
            if (files[i].getName().indexOf(".mp3") != -1) {

                Map<String, Object> filesMap = new HashMap<>();
                names.add(files[i].getName());//加入檔案名字
                paths.add(files[i].getPath());//加入路徑(KEY，VALUE)

                filesMap.put(IMG_ITEM, fileImg[1]);
                filesMap.put(NAME_ITEM, files[i].getName());
                filesList.add(filesMap);
            }
        }
    }

    //控制播放以及轉換曲目
    class PlayingThread extends Thread {
        @Override
        public void run() {
            int a = player.play();
            if (a == 0 || a == -1)
                Music.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
                        player.pause();
                        //player.setPosition(0);
                        int index = paths.indexOf(nowPath) + 1;
                        if (index < paths.size()) {
                            initPlayerAndGo(paths.get(index), true);
                        } else {
                            initPlayerAndGo(nowPath, false);
                        }
                    }
                });
        }
    }

    private class DurationBarListener implements SeekBar.OnSeekBarChangeListener {
        boolean pause;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            txvTime.setText(player.getCurrentPositionFormatted());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            pause = !player.isPlaying();
            player.pause();

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int pos = seekBar.getProgress();
            player.setPosition(pos / player.getMsPerFrame());
            txvTime.setText(player.getCurrentPositionFormatted());
            if (!pause) {
                playing = new PlayingThread();
                playing.start();
            }

        }
    }

    private class ButtonSetListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int index;
            switch (v.getId()) {
                case R.id.play:
                    if (player.isPlaying()) {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.play));
                        player.pause();
                    } else {
                        playButton.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                        playing = new PlayingThread();
                        playing.start();
                    }
                    break;
                case R.id.previous:
                    index = paths.indexOf(nowPath) - 1;
                    if (index < 0) index = 0;
                    initPlayerAndGo(paths.get(index), player.isPlaying());
                    break;
                case R.id.next:
                    index = paths.indexOf(nowPath) + 1;
                    if (index >= paths.size())
                        index = 0;
                    initPlayerAndGo(paths.get(index), player.isPlaying());
                    break;
                case R.id.effect:
                    effectSettingDialog.show();
                    break;
                case R.id.bluetooth:
                    if(!ledCubeController.isOpen())
                    {
                        ledCubeController.open(Music.this);
                        return;
                    }
                    if (!ledCubeController.isConnected()) {
                        pairedListDialog.update(ledCubeController.listBoundedDevicesName());
                        pairedListDialog.show();
                    } else {
                        ledCubeController.disconnect();
                        bluetoothButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_white_36dp));
                        player.removePlayingAction(ledCubeMotion);
                    }
            }
        }
    }

    private class PairedListListener implements AdapterView.OnItemClickListener {
        boolean connected = false;
        Activity act;

        PairedListListener(Activity c) {
            this.act = c;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            connected = false;
            String str = (String) view.getTag();
            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            //addLog("與" + str + "連線");
            ledCubeController.clean();
            ledCubeController.establishSocket(str);

            pairedListDialog.close();
            Thread thread = new Thread() {
                public void run() {
                    if (!ledCubeController.connect()) {
                        connected = false;
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "連線失敗", Toast.LENGTH_SHORT).show();
                                bluetoothButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_white_36dp));
                            }
                        });
                    } else {
                        //Toast.makeText(getApplicationContext(), "連線成功", Toast.LENGTH_SHORT).show();
                        connected = true;
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "連線成功", Toast.LENGTH_SHORT).show();
                                bluetoothButton.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_blue));
                                player.addPlayingAction(ledCubeMotion);
                            }
                        });
                    }
                }
            };
            thread.start();

        }
    }

    private class FileListener extends FileObserver {
        public FileListener(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            switch (event){
                case CREATE: case DELETE:
                    System.out.println(path);
                    break;
            }
        }
    }
}