package com.ozner.qianye.CChat;

import android.content.Context;

import com.ozner.qianye.ACSqlLite.CSqliteDb;
import com.ozner.qianye.CChat.bean.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ozner_67 on 2016/4/11.
 */
public class ChatMessageHelper {
    public String TableName;
    public String MessageList = "messagelist";

    public static class ChatHolder {
        public static final ChatMessageHelper INSTANCE = new ChatMessageHelper();
    }

    public ChatMessageHelper() {
    }

    public static final ChatMessageHelper getInstance(String tableName) {
        ChatHolder.INSTANCE.TableName = tableName.replace("-", "");
        return ChatHolder.INSTANCE;
    }

    public void InitTable(final Context context) {
        String Sql = String.format("CREATE TABLE IF NOT EXISTS %s (_id integer PRIMARY KEY AutoIncrement,content TEXT,oper integer,isSendSus integer,time integer)", MessageList + TableName);
        CSqliteDb.execSQLNonQuery(context, Sql, new String[]{});
    }

    /*
    *插入一条聊天记录
     */
    public void InsertMessage(Context context, ChatMessage chatMessage) {
        if (context != null && TableName != null) {
            String sql = String.format("insert or replace into %s(content,oper,isSendSus,time)values(?,?,?,?)", MessageList + TableName);
            CSqliteDb.execSQLNonQuery(context, sql, new Object[]{chatMessage.getContent(), chatMessage.getOper(), chatMessage.getIsSendSuc(), chatMessage.getTime()});
        }
    }

    //获取聊天列表
    public List<ChatMessage> getMessageList(final Context context) {
        if (context != null && TableName != null) {
            String sql = String.format("select * from %s order by time asc", MessageList + TableName);
            List<String[]> result = CSqliteDb.ExecSQL(context, sql, new String[]{});
            if (result != null && result.size() > 0) {
                List<ChatMessage> chatMessageList = new ArrayList<>();
                for (String[] row : result) {
                    try {
                        ChatMessage message = new ChatMessage();
                        message.setContent(row[1]);
                        message.setOper(Integer.parseInt(row[2]));
                        message.setIsSendSuc(Integer.parseInt(row[3]));
                        message.setTime(Long.parseLong(row[4]));
                        chatMessageList.add(message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }
                }
                return chatMessageList;
            }
        }
        return null;
    }

    //更新已有记录，没有的话插入新的数据
    public void updateChatMsg(final Context context, ChatMessage chatMessage) {
        if (context != null && TableName != null) {
            String sql = String.format("update %s set isSendSus = %s where time=%s", MessageList + TableName, chatMessage.getIsSendSuc(), chatMessage.getTime());
            CSqliteDb.execSQLNonQuery(context, sql, new Object[]{});
        }
    }
}
