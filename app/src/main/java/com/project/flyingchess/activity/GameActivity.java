package com.project.flyingchess.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.peak.salut.SalutDevice;
import com.project.flyingchess.R;
import com.project.flyingchess.dialog.PeersDialog;
import com.project.flyingchess.dialog.StartGameDialog;
import com.project.flyingchess.dialog.WaitingPlayerDialog;
import com.project.flyingchess.dialog.WinnerDialog;
import com.project.flyingchess.eventbus.UpdateDiceEvent;
import com.project.flyingchess.eventbus.UpdateGameInfoEvent;
import com.project.flyingchess.eventbus.UpdateTimeEvent;
import com.project.flyingchess.eventbus.UpdateTitleEvent;
import com.project.flyingchess.eventbus.WinnerEvent;
import com.project.flyingchess.player.AIPalayer;
import com.project.flyingchess.player.LocalPalayer;
import com.project.flyingchess.player.Player;
import com.project.flyingchess.ruler.ClientRuler;
import com.project.flyingchess.ruler.DefaultRuler;
import com.project.flyingchess.ruler.IRuler;
import com.project.flyingchess.ruler.ServerRuler;
import com.project.flyingchess.utils.Color;
import com.project.flyingchess.widget.ChessBoard;
import com.project.flyingchess.widget.UpMarqueeTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = "GameActivity";
    private Context mContext;

    private IRuler ruler;
    private List<Player> mList;

    public static final String KEY_MODE = "MODE";
    private int MODE = SINGLE;
    public static final int SINGLE = 0;
    public static final int NET_SERVER = SINGLE + 1;
    public static final int NET_CLIENT = SINGLE + 2;

    private ChessBoard v_chessBoard;
    private TextView tv_time;
    private UpMarqueeTextView tv_title;
    private ImageView iv_dice;

    private WinnerDialog winnerDialog;
    private StartGameDialog startGameDialog;
    private WaitingPlayerDialog waitingPlayerDialog;
    private PeersDialog peersDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mContext = this;

        EventBus.getDefault().register(this);

        MODE = getIntent().getIntExtra(KEY_MODE,SINGLE);

        findview();
        init();
    }

    private void init() {
        switch (MODE){
            case SINGLE:
                mList = new ArrayList<>();

                int player_1 = getIntent().getIntExtra(ConfigueActivity.PLAYER_1,SINGLE);
                int player_2 = getIntent().getIntExtra(ConfigueActivity.PLAYER_2,SINGLE);
                int player_3 = getIntent().getIntExtra(ConfigueActivity.PLAYER_3,SINGLE);
                int player_4 = getIntent().getIntExtra(ConfigueActivity.PLAYER_4,SINGLE);

                if(player_1 != ConfigueActivity.NONE)
                    mList.add(judgePlayer(player_1,ConfigueActivity.PLAYER_1,Color.BLUE));
                if(player_2 != ConfigueActivity.NONE)
                    mList.add(judgePlayer(player_2,ConfigueActivity.PLAYER_2,Color.YELLOW));
                if(player_3 != ConfigueActivity.NONE)
                    mList.add(judgePlayer(player_3,ConfigueActivity.PLAYER_3,Color.RED));
                if(player_4 != ConfigueActivity.NONE)
                    mList.add(judgePlayer(player_4,ConfigueActivity.PLAYER_4,Color.GREEN));

                ruler = new DefaultRuler(mList);
                ruler.start();

                break;
            case NET_SERVER:
            case NET_CLIENT:
                initStartGameDialog();
                initWaitingPlayerDialog();
                initPeersDialog();
                break;
            default:
                Logger.d("莫名其妙的错误啊~亲。 ");
                break;
        }

        initWinnerDialog();
    }

    private void initWinnerDialog() {
        winnerDialog = new WinnerDialog(GameActivity.this, R.style.NoTitleDialog);
        winnerDialog.setCanceledOnTouchOutside(false
        );
        winnerDialog.setRestartListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"restart~",Toast.LENGTH_SHORT).show();
                if(ruler != null) ruler.restart();
                if(v_chessBoard != null) v_chessBoard.restart();
                winnerDialog.dismiss();
            }
        });
    }

    private void initPeersDialog() {
        peersDialog = new PeersDialog(GameActivity.this, R.style.NoTitleDialog);
        peersDialog.setCanceledOnTouchOutside(false);
        peersDialog.setCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peersDialog.dismiss();
                startGameDialog.show();
            }
        });
    }

    private void initWaitingPlayerDialog() {
        waitingPlayerDialog = new WaitingPlayerDialog(GameActivity.this, R.style.NoTitleDialog);
        waitingPlayerDialog.setCanceledOnTouchOutside(false);
        waitingPlayerDialog.setBeginListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"begin~",Toast.LENGTH_SHORT).show();
                ruler.start();
            }
        });
        waitingPlayerDialog.setCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ruler.uninit();

                waitingPlayerDialog.dismiss();
                startGameDialog.show();
            }
        });
    }

    private void initStartGameDialog() {
        startGameDialog = new StartGameDialog(GameActivity.this, R.style.NoTitleDialog);
        startGameDialog.setCanceledOnTouchOutside(false);
        startGameDialog.setCreateGameListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"start game~",Toast.LENGTH_SHORT).show();
                ruler = new ServerRuler(mContext,new LocalPalayer(ConfigueActivity.PLAYER_1,Color.BLUE,v_chessBoard));

                startGameDialog.dismiss();
                waitingPlayerDialog.show();
            }
        });

        startGameDialog.setJoinGameListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"join game~",Toast.LENGTH_SHORT).show();
                //ruler = new ServerRuler(mContext,new LocalPalayer(ConfigueActivity.PLAYER_1,Color.BLUE,v_chessBoard));
                ruler = new ClientRuler(mContext,new LocalPalayer("T>T", Color.NONE, v_chessBoard));

                startGameDialog.dismiss();
                peersDialog.show();
            }
        });

        startGameDialog.setCancelListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGameDialog.dismiss();
                finish();
            }
        });

        startGameDialog.show();
    }

    private Player judgePlayer(int num,String name,int color) {
        Player player = null;
        switch (num){
            case ConfigueActivity.HUMAN:
                player = new LocalPalayer(name, color, v_chessBoard);
                break;
            case ConfigueActivity.COMPUTER:
                player = new AIPalayer(name, color, v_chessBoard);
                break;
            /*case ConfigueActivity.NONE:
                player = new NonePlayer(name, Color.NONE);
                break;*/
            default:
                break;
        }
        return player;
    }

    private void findview() {
        v_chessBoard = (ChessBoard) findViewById(R.id.v_chessboard);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_title = (UpMarqueeTextView) findViewById(R.id.tv_title);
        iv_dice = (ImageView) findViewById(R.id.iv_dice);

        iv_dice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_dice:
                if(ruler != null)
                    ruler.dice();
                break;

            default:
                break;
        }
    }

    @Subscribe
    public void onEventMainThread(UpdateTimeEvent msg) {
        tv_time.setText(msg.getMsgContent());
    }

    @Subscribe
    public void onEventMainThread(UpdateDiceEvent msg) {
        switch (msg.getNumber()){
            case 1:
                iv_dice.setImageResource(R.drawable.d1);
                break;
            case 2:
                iv_dice.setImageResource(R.drawable.d2);
                break;
            case 3:
                iv_dice.setImageResource(R.drawable.d3);
                break;
            case 4:
                iv_dice.setImageResource(R.drawable.d4);
                break;
            case 5:
                iv_dice.setImageResource(R.drawable.d5);
                break;
            case 6:
                iv_dice.setImageResource(R.drawable.d6);
                break;
            default:
                iv_dice.setImageResource(R.drawable.icon_dice);
                break;
        }
    }

    @Subscribe
    public void onEventMainThread(WinnerEvent msg) {
        String[] players = msg.getMsgContent().split(",");
        if(players.length >= 1)
            winnerDialog.setFirst(players[0]);
        else
            winnerDialog.setFirst("");

        if(players.length >= 2)
            winnerDialog.setSecond(players[1]);
        else
            winnerDialog.setSecond("");
        if(players.length >= 3)
            winnerDialog.setThird(players[2]);
        else
            winnerDialog.setThird("");
        winnerDialog.setBtnVisible(false);
        winnerDialog.show();
    }

    @Subscribe
    public void onEventMainThread(UpdateTitleEvent msg) {
        tv_title.setText(msg.getMsgContent());
    }

    @Subscribe
    public void onEventMainThread(UpdateGameInfoEvent msg) {
        waitingPlayerDialog.setContent(msg.getMsgContent());
    }

    @Subscribe
    public void onEventMainThread(List<Player> players) {
        if(players.size() >= 1)
            winnerDialog.setFirst(players.get(0).getName());
        else
            winnerDialog.setFirst("");

        if(players.size() >= 2)
            winnerDialog.setSecond(players.get(1).getName());
        else
            winnerDialog.setSecond("");
        if(players.size() >= 3)
            winnerDialog.setThird(players.get(2).getName());
        else
            winnerDialog.setThird("");

        winnerDialog.setBtnVisible(true);
        winnerDialog.show();
    }

    @Subscribe
    public void onEventMainThread(ArrayList<SalutDevice> devices) {
        peersDialog.updateView(devices);
    }

    @Subscribe
    public void onEventMainThread(SalutDevice device) {
        peersDialog.dismiss();
        ((ClientRuler)ruler).setServerDevice(device);//这样写真的好嘛~？
        ruler.start();
    }

    /*public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            //TODO~就是不让你退出~怎么样怎么样怎么样~？打我啊.
            //Toast.makeText(mContext,getString(R.string.key_back_slogan),Toast.LENGTH_LONG).show();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(ruler != null) ruler.uninit();
        startGameDialog.dismiss();
    }

    public String getTag() {
        return TAG;
    }
}
