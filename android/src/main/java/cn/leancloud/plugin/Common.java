package cn.leancloud.plugin;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.AVException;
import cn.leancloud.AVFile;
import cn.leancloud.im.Signature;
import cn.leancloud.im.v2.AVIMBinaryMessage;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMTypedMessage;
import cn.leancloud.im.v2.messages.AVIMFileMessage;
import cn.leancloud.im.v2.messages.AVIMLocationMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.StringUtil;
import io.flutter.plugin.common.MethodCall;

public class Common {
  public static final String Method_Close_Client = "closeClient";
  public static final String Method_Open_Client = "openClient";
  public static final String Method_Create_Conversation = "createConversation";
  public static final String Method_Fetch_Conversation = "getConversation";
  public static final String Method_Send_Message = "sendMessage";
  public static final String Method_Read_Message = "readMessage";
  public static final String Method_Update_Message = "updateMessage";
  public static final String Method_Get_Message_Receipt = "getMessageReceipt";
  public static final String Method_Query_Message = "queryMessage";
  public static final String Method_Update_Members = "updateMembers";
  public static final String Method_Mute_Conversation = "muteToggle";
  public static final String Method_Update_Conversation = "updateData";
  public static final String Method_Online_Member_Count = "getOnlineMembersCount";

  public static final String Method_Client_Offline = "";
  public static final String Method_Client_Disconnected = "";
  public static final String Method_Client_Resumed = "";

  public static final String Method_Message_Received = "onMessageReceive";
  public static final String Method_Message_Receipted = "onMessageReceipt";
  public static final String Method_Message_updated = "onMessageUpdate";

  public static final String Param_Client_Id = "clientId";
  public static final String Param_Force_Open = "force";
  public static final String Param_Client_Tag = "tag";
  public static final String Param_Signature = "sign";
  public static final String Param_Conv_Type = "conv_type";
  public static final String Param_Conv_Members = "m";
  public static final String Param_Conv_Name = "name";
  public static final String Param_Conv_Attributes = "attr";
  public static final String Param_Conv_TTL = "ttl";

  public static final String Param_code = "code";

  public static final int Conv_Type_Unique = 0;
  public static final int Conv_Type_Common = 1;
  public static final int Conv_Type_Transient = 2;
  public static final int Conv_Type_Temporary = 4;

  public static JSONObject wrapException(AVException ex) {
    JSONObject result = new JSONObject();
    result.put("code", ex.getCode());
    result.put("message", ex.getMessage());
    return result;
  }

  public static <T> T getMethodParam(MethodCall call, String key) {
    if (call.hasArgument(key)) {
      return call.argument(key);
    }
    return null;
  }

  public static boolean getParamBoolean(MethodCall call, String key) {
    if (call.hasArgument(key)) {
      return call.argument(key);
    }
    return false;
  }

  public static int getParamInt(MethodCall call, String key) {
    if (call.hasArgument(key)) {
      return call.argument(key);
    }
    return 0;
  }

  public static Signature getMethodSignature(MethodCall call, String key) {
    Map<String, Object> param = getMethodParam(call, key);
    if (null == param) {
      return null;
    }
    Signature result = new Signature();
    result.setSignature((String) param.get("s"));
    result.setNonce((String) param.get("n"));
    result.setTimestamp((long) param.get("t"));
    return result;
  }

  public static Map<String, Object> wrapClient(AVIMClient client) {
    HashMap<String, Object> result = new HashMap<>();
    result.put("clientId", client.getClientId());
    return result;
  }

  public static Map<String, Object> wrapTypedMessage(AVIMTypedMessage message) {
    HashMap<String, Object> result = new HashMap<>();
    result.put("_lctype", message.getMessageType());

    String text = null;
    Map<String, Object> attributes = null;
    Map<String, Object> fileInstance = null;
    Map<String, Object> locationInstance = null;
    if (message instanceof AVIMTextMessage) {
      text = ((AVIMTextMessage)message).getText();
      attributes = ((AVIMTextMessage)message).getAttrs();
    } else if (message instanceof AVIMFileMessage) {
      text = ((AVIMFileMessage) message).getText();
      attributes = ((AVIMFileMessage) message).getAttrs();
      AVFile avFile = ((AVIMFileMessage) message).getAVFile();

      fileInstance = new HashMap<String, Object>();
      if (null != avFile) {
        fileInstance.put("objId", avFile.getObjectId());
        fileInstance.put("url", avFile.getUrl());
      }
      fileInstance.put("metaData", ((AVIMFileMessage) message).getFileMetaData());
    } else if (message instanceof AVIMLocationMessage) {
      text = ((AVIMLocationMessage) message).getText();
      attributes = ((AVIMLocationMessage) message).getAttrs();
      AVGeoPoint geoPoint = ((AVIMLocationMessage) message).getLocation();
      if (null != geoPoint) {
        locationInstance = new HashMap<>();
        locationInstance.put("latitude", geoPoint.getLatitude());
        locationInstance.put("longitude", geoPoint.getLongitude());
      }
    } else {
      text = message.getContent();
    }
    if (!StringUtil.isEmpty(text)) {
      result.put("_lctext", text);
    }
    if (null != attributes) {
      result.put("_lcattrs", attributes);
    }
    if (null != fileInstance) {
      result.put("_lcfile", fileInstance);
    }
    if (null != locationInstance) {
      result.put("_lcloc", locationInstance);
    }
    return result;
  }

  public static Map<String, Object> wrapMessage(AVIMMessage message) {
    Map<String, Object> result = new HashMap<>();
    if (!StringUtil.isEmpty(message.getMessageId())) {
      result.put("id", message.getMessageId());
    }
    String conversationId = message.getConversationId();
    if (!StringUtil.isEmpty(conversationId)) {
      result.put("cid", conversationId);
    }
    String from = message.getFrom();
    if (!StringUtil.isEmpty(from)) {
      result.put("from", from);
    }
    long timestamp = message.getTimestamp();
    if (timestamp > 0l) {
      result.put("timestamp", timestamp);
    }
    long patchTimestamp = message.getUpdateAt();
    if (patchTimestamp > 0l) {
      result.put("patchTimestamp", patchTimestamp);
    }
    long deliverTimestamp = message.getDeliveredAt();
    if (deliverTimestamp > 0l) {
      result.put("ackAt", deliverTimestamp);
    }
    long readTimestamp = message.getReadAt();
    if (readTimestamp > 0l) {
      result.put("readAt", readTimestamp);
    }
    boolean mentionAll = message.isMentionAll();
    if (mentionAll) {
      result.put("mentionAll", mentionAll);
    }
    List<String> mentionList = message.getMentionList();
    if (null != mentionList && mentionList.size() > 0) {
      result.put("mentionPids", mentionList);
    }
    if (message instanceof AVIMTypedMessage) {
      AVIMTypedMessage typedMessage = (AVIMTypedMessage) message;
      result.put("typeMsgData", wrapTypedMessage(typedMessage));
    } else if (message instanceof AVIMBinaryMessage) {
      AVIMBinaryMessage binaryMessage = (AVIMBinaryMessage) message;
      result.put("binaryMsg", binaryMessage.getBytes());
    } else {
      String content = message.getContent();
      if (null != content) {
        result.put("msg", content);
      }
    }

    return result;
  }

  public static Map<String, Object> wrapConversation(AVIMConversation conversation) {
    HashMap<String, Object> result = new HashMap<>();
    String convId = conversation.getConversationId();
    String creator = conversation.getCreator();
    result.put("id", convId);
    result.put("client", creator);
    return result;
  }
}
