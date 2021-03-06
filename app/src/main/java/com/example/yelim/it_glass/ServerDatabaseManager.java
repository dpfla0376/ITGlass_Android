package com.example.yelim.it_glass;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Yelim on 2017-03-25.
 */

/**
 * Firebase 상의 Realtime Database와 연동하여 Data를 관리
 */
public class ServerDatabaseManager {
    private static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference mDatabaseReference;
    private static ChildEventListener mChildEventListener;
    private static Object value;
    private static String userID;
    private static int userDrink;
    private static int flag = 0;
    private static List<Friend> friendList = new ArrayList<Friend>();
    private static ArrayList<String> tempFriendDrink = new ArrayList<String>();
    private static String realYear;
    private static String realMonth;
    private static String realDay;
    private static String realHour;
    private static String realMin;
    private static String serverYear;
    private static String serverMonth;
    private static String serverDay;
    private static String serverHour;
    private static String serverMin;
    private static boolean isDateChanged;
    private static Callback callback;
    private static Callback innerCallback;
    private static Callback hasIDCallback;
    private static Callback settingCallback;
    //private ArrayAdapter<String> mAdapter;

    ServerDatabaseManager() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    /**
     * 입력받은 ID가 user로 등록되었는지 확인
     * 유저 등록 시 중복 검사 & 친구 등록 시 존재 여부 확인
     * 등록 : true
     * 비등록 : false
     *
     * @param ID
     * @return
     */
    public static void hasID(String ID) {

        //access [ user_list ] line in firebase
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");

        //access to value of which key = ID in user_list of firebase
        mDatabaseReference.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                value = dataSnapshot.getValue(Object.class);
                if(value == null) hasIDCallback.callBackMethod(false);
                else hasIDCallback.callBackMethod(true);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * 입력받은 userID를 user로 서버에 등록
     *
     * @param userID
     */
    public static void saveUserID(String userID) {
        //access [ user_list ] line in firebase
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");

        mDatabaseReference.child(userID).child("drink").child("amount").setValue("0");
        mDatabaseReference.child(userID).child("drink").child("timing").setValue(0);
        mDatabaseReference.child(userID).child("friend_list").setValue(null);
        mDatabaseReference.child(userID).child("last_access").setValue("0000/00/00/00:00");

        ServerDatabaseManager.setLastAccessTime();
        userDrink = 0;
    }

    /**
     * 현재 기기의 userID를 저장
     *
     * @param ID
     */
    public static void setLocalUserID(String ID) {
        userID = ID;
    }

    public static void setLocalUserDrink(int drink) {
        userDrink = drink;
    }

    /**
     * 현재 기기의 userID를 반환
     *
     * @return
     */
    public static String getLocalUserID() {
        return userID;
    }

    public static int getLocalUserDrink() { return userDrink; }

    /**
     * userID의 친구로 friendID를 추가
     * light값이 없으므로 default값 255/255/255 로 저장됨
     *
     * @param userID
     * @param friendID
     * @return
     */
    public static void addFriend(final String userID, final String friendID) {
        hasID(friendID);
        hasIDCallback = new Callback() {
            @Override
            public void callBackMethod() {

            }

            @Override
            public void callBackMethod(boolean value) {
                if (value) {
                    //access [ friend_list ] line in firebase
                    mDatabaseReference = mFirebaseDatabase.getReference("user_list");
                    mDatabaseReference.child(userID).child("friend_list").child(friendID).setValue("255.255.255");
                }
                //there is no friendID
                else {
                    Log.e("ADD_FRIEND_FIREBASE", "-------friendID not user-------");
                }
            }
        };
        hasIDCallback.callBackMethod();

    }

    /**
     * userID의 친구로 friendID를 추가
     * light값은 R/G/B 로 저장됨
     *
     * @param userID
     * @param friendID
     * @param R
     * @param G
     * @param B
     * @return
     */
    public static void addFriend(final String userID, final String friendID, final int R, final int G, final int B) {
        hasID(friendID);
        hasIDCallback = new Callback() {
            @Override
            public void callBackMethod() {

            }

            @Override
            public void callBackMethod(boolean value) {
                if (value) {
                    //access [ friend_list ] line in firebase
                    mDatabaseReference = mFirebaseDatabase.getReference("user_list");
                    mDatabaseReference.child(userID).child("friend_list").child(friendID).setValue(R + "." + G + "." + B + "");
                }
                //there is no friendID
                else {
                    Log.e("ADD_FRIEND_FIREBASE", "-------friendID not user-------");
                }
            }
        };
        hasIDCallback.callBackMethod();
    }

    /**
     * userID의 친구 중 friendID를 삭제
     *
     * @param userID
     * @param friendID
     */
    public static void deleteFriend(String userID, String friendID) {
        //access [ friend_list ] line in firebase
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        mDatabaseReference.child(userID).child("friend_list").child(friendID).removeValue();

    }

    /**
     * userID의 친구 중 friendID의 light값을 R/G/B 로 변경
     *
     * @param userID
     * @param friendID
     * @param R
     * @param G
     * @param B
     */
    public static void changeLightColor(String userID, String friendID, int R, int G, int B) {
        //deleteFriend(userID, friendID);
        addFriend(userID, friendID, R, G, B);

    }

    /**
     * 현재 기기의 userID의 friendList를 받아옴
     *
     * @return List<Friend> friendList
     */
    public static List<Friend> getFriendList() {
        return friendList;
    }

    /**
     * 현재 기기의 userID의 friend 전부를 서버에서 받아와 friendList에 저장
     *
     * @param userID
     */
    public static void getFriend(String userID) {
        Log.d("SERVER_DBM", "In GetFriend Method");
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        Log.d("SERVER_DBM", "GetReferenceSuccessful");
        try {
            if (mDatabaseReference.child(userID).child("friend_list").getKey() != null) {
                mDatabaseReference.child(userID).child("friend_list").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Log.d("SERVER_DBM", "-------ValueEvent");
                        Log.d("SERVER_DBM", "FriendListSize : " + dataSnapshot.getChildrenCount());
                        collectFriends((Map<String, String>) dataSnapshot.getValue());
                        for (int i = 0; i < friendList.size(); i++)
                            Log.d("FRIEND_LIST", friendList.get(i).getfID() + ":" + friendList.get(i).getfLight());
                        callback.callBackMethod();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } catch(NullPointerException e) {
            Log.e("MainActivity", "-------Firebase Database NULL!");
            
        }
    }

    private static ChildEventListener myEventListener(final String friendID, final String light) {
        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // 술을 마시는 모션
                if (dataSnapshot.getKey().equals("timing") && dataSnapshot.getValue(Long.class) == 1) {
                    Log.d("SERVER_DBM", "------- event from " + friendID + " / light " + light + " / drinking");
                    BluetoothManager.writeData(light);
                }
                // 모션 끝. 다시 원상태로 복귀.
                else if (dataSnapshot.getKey().equals("timing") && dataSnapshot.getValue(Long.class) == 0) {
                    Log.d("SERVER_DBM", "------- event from " + friendID + " / light " + light + " / stop drinking");
                }
                // 음주량 값 변경
                else {
                    Log.d("SERVER_DBM", "------ " + friendID + " drink amount changed");
                    for(int i=0; i<friendList.size(); i++) {
                        if(friendID.equals(friendList.get(i).getfID())) {
                            friendList.get(i).setfDrink(dataSnapshot.getValue(String.class));
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //String message = dataSnapshot.getValue(String.class);
                //mAdapter.remove(message);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return eventListener;
    }

    /**
     * Firebase의 database에서 얻어온 data를 형식에 맞도록 조정
     *
     * @param friends
     */
    private static void collectFriends(Map<String, String> friends) {
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        friendList.clear();
        if (friends != null) {
            for (Map.Entry<String, String> entry : friends.entrySet()) {
                if(mDatabaseReference.child(entry.getKey()).child("friend_list").getKey() != null) {
                    Log.e("mDatabaseReference", "---------" + mDatabaseReference.child(entry.getKey()).child("friend_list").getKey().toString());
                    mDatabaseReference.child(entry.getKey()).child("drink").addChildEventListener(myEventListener(entry.getKey(), entry.getValue()));
                    friendList.add(new Friend(entry.getKey(), entry.getValue()));
                }
                else {
                    //Log.e("SERVER_DBM", "----friend " + entry.getKey() + " is not a user");
                    //mDatabaseReference.child(userID).child("friend_list").child(entry.getKey()).removeValue();
                    //삭제된 user가 친구목록에 있음. 리스너 달지 말고 친구목록에서 제거
                }
            }
        }
    }

    public static void getFriendDrinkAmount(final String friendID) {
        if(mDatabaseReference.child(friendID) != null) {
            mDatabaseReference.child(friendID).child("drink").child("amount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.d("SERVER_DBM", "-------FriendDrinkValueEvent");
                    if (dataSnapshot.getValue(String.class) != null) {
                        tempFriendDrink.add(dataSnapshot.getValue(String.class));
                        Log.d("SERVER_DBM", "-------" + friendID + " : " + tempFriendDrink + " ml");
                        flag++;
                    }
                    else {
                        Log.e("SERVER_DBM", "----friend " + friendID + " is not a user");
                        for(int i=0; i<friendList.size(); i++) {
                            if(friendList.get(i).getfID().equals(friendID)) friendList.remove(i);
                        }
                        mDatabaseReference.child(userID).child("friend_list").child(friendID).removeValue();
                    }
                    innerCallback.callBackMethod();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            //Log.e("SERVER_DBM", "----friend " + friendID + " is not a user");
            //mDatabaseReference.child(userID).child("friend_list").child(friendID).removeValue();
            //삭제된 user가 친구목록에 있음. 리스너 달지 말고 친구목록에서 제거
        }
    }


    /**
     * 서버에서 사용자의 친구들의 음주량을 받아와 tempFriendDrink 리스트(=buffer)에 저장.
     */
    public static void getFriendListDrinkAmount() {
        //tempFriendDrink.clear();
        for (int i = 0; i < ServerDatabaseManager.getFriendList().size(); i++) {
            final int j = i;
            ServerDatabaseManager.getFriendDrinkAmount(friendList.get(i).getfID());
            Callback inCallBack = new Callback() {
                @Override
                public void callBackMethod() {
                    Log.d("ServerDatabaseManager", "---------------in callBackMethod");
                }

                @Override
                public void callBackMethod(boolean value) {

                }


            };

            ServerDatabaseManager.setInnerCallBack(inCallBack);
        }
    }

    /**
     * tempFriendDrink 버퍼의 값을 실제 friendList에 복사.
     */
    public static void setFriendListDrinkAmount() {
        int j = getFriendList().size();
        Log.d("SERVER_DBM", "friendListSize=" + j);
        for (int i = 0; i < j; i++) {
            String temp = tempFriendDrink.get(i);
            friendList.get(i).setfDrink(temp);
            j = getFriendList().size();
            Log.d("SERVER_DBM", "friendListSize=" + j);
        }
    }

    public static int getFlag() {
        return flag;
    }

    public static void setFlag(int f) {
        flag = f;
    }

    public static ArrayList<String> getTempFriendDrink() {
        return tempFriendDrink;
    }

    public static String getTempFriendDrink(int position) {
        return tempFriendDrink.get(position);
    }

    public static void clearTempFriendDrink() {
        tempFriendDrink.clear();
        Log.d("SERVER_DBM", "tempFriendDrinkSize=" + tempFriendDrink.size());
    }

    /**
     * 술을 마신다 로 변경
     */
    public static void turnOnDrinkTiming() {
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        mDatabaseReference.child(userID).child("drink").child("timing").setValue(1);
    }

    /**
     * 술을 안마신다 로 변경
     */
    public static void turnOffDrinkTiming() {
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        mDatabaseReference.child(userID).child("drink").child("timing").setValue(0);
    }

    /**
     * 음주량 노출 설정이 on 일 경우 음주량 변경을 서버에 업데이트
     * @param amount
     */
    public static void setServerDrinkAmount(String amount) {
        if(DatabaseManager.isDrinkOn) {
            mDatabaseReference = mFirebaseDatabase.getReference("user_list");
            mDatabaseReference.child(userID).child("drink").child("amount").setValue(amount);
        }
    }

    /**
     * 마지막으로 앱을 시작한 시간을 서버에 갱신
     */
    public static void setLastAccessTime() {
        setTime();
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        mDatabaseReference.child(userID).child("last_access").setValue(realYear + "/" + realMonth + "/" + realDay + "/" + realHour + ":" + realMin);
    }

    /**
     * 처음 app 시작 시 server database 값을 초기화 및 이전 데이터를 local database에 저장
     */
    public static void resetServerDB() {

        mDatabaseReference.child(userID).child("drink").child("amount").setValue("0");
    }

    /**
     * Firebase에 존재하는 userID 정보를 삭제
     */
    public static void deleteServerData() {
        //access [ friend_list ] line in firebase
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        mDatabaseReference.child(userID).removeValue();
    }

    private static void getLastAccessTime() {
        mDatabaseReference = mFirebaseDatabase.getReference("user_list");
        mDatabaseReference.child(userID).child("last_access").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String temp = (String) dataSnapshot.getValue();
                String[] tempList = temp.split("/");
                serverYear = tempList[0];
                serverMonth = tempList[1];
                serverDay = tempList[2];
                String[] tempTemp = tempList[3].split(":");
                serverHour = tempTemp[0];
                serverMin = tempTemp[1];
                callback.callBackMethod();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * 마지막 앱 시작 시간으로부터 날이 바뀌면 true, 같은 날 접속하면 false
     * @return
     */
    public static void isDateChanged() {
        getLastAccessTime();
        Callback callback = new Callback() {
            @Override
            public void callBackMethod() {
                Log.d("isDateChanged", "callbackMethod");
                setLastAccessTime();
                if(Integer.parseInt(realYear + realMonth + realDay) > Integer.parseInt(serverYear + serverMonth + serverDay))
                    isDateChanged =  true;
                else if(Integer.parseInt(realYear + realMonth + realDay) < Integer.parseInt(serverYear + serverMonth + serverDay)) isDateChanged = false;
                else {
                    Log.e("ERROR", "---- date value was worng!!");
                    isDateChanged =  true;
                }
                settingCallback.callBackMethod(isDateChanged);
            }

            @Override
            public void callBackMethod(boolean value) {

            }
        };
        ServerDatabaseManager.setCallBack(callback);

    }

    /**
     * 현재 시간 계산
     */
    private static void setTime() {
        Calendar c = Calendar.getInstance();

        realYear = c.get(Calendar.YEAR) + "";
        if(c.get(Calendar.MONTH) >=0 && c.get(Calendar.MONTH) < 9) {
            realMonth = "0" + (c.get(Calendar.MONTH)+1) + "";
        }
        else {
            realMonth = (c.get(Calendar.MONTH)+1) + "";
        }

        if(c.get(Calendar.DATE) >=0 && c.get(Calendar.DATE) < 10) {
            realDay = "0" + c.get(Calendar.DATE) + "";
        }
        else {
            realDay =  c.get(Calendar.DATE) + "";
        }

        realHour = c.get(Calendar.HOUR_OF_DAY) + "";
        int temp = c.get(Calendar.MINUTE);
        if(temp >= 0 && temp < 10) realMin = "0" + temp;
        else realMin = temp + "";

        Log.d("access_time", realYear + "/" + realMonth + "/" + realDay + "/" + realHour + ":" + realMin);
    }

    public static String getTime() {
        setTime();
        return realYear + "/" + realMonth + "/" + realDay + "/" + realHour + ":" + realMin;
    }

    /**
     * Firebase의 Realtime Database에서 값을 얻어올 때 asynchronous 하므로 callback을 이용
     *
     * @param callBack
     */
    public static void setCallBack(Callback callBack) {
        callback = callBack;
    }

    public static void setInnerCallBack(Callback callback) {
        innerCallback = callback;
    }

    public static void sethasIDCallback(Callback callback) {
        hasIDCallback = callback;
    }

    public static void setSettingCallback(Callback callback) { settingCallback = callback; }

    public static Callback gethasIDCallback() {
        return hasIDCallback;
    }
}
