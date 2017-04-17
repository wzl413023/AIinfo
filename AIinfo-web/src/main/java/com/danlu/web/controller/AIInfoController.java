package com.danlu.web.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.danlu.dleye.core.ArticleInfoManager;
import com.danlu.dleye.core.util.DleyeSwith;
import com.danlu.dleye.core.util.RedisClient;
import com.danlu.dleye.persist.base.ArticleInfo;

@Controller
public class AIInfoController implements Serializable {

    private static final long serialVersionUID = -908534251L;
    private static Logger logger = LoggerFactory.getLogger(AIInfoController.class);
    @Autowired
    private ArticleInfoManager articleInfoManager;
    @Autowired
    private DleyeSwith dleyeSwith;
    @Autowired
    private RedisClient redisClient;

    @RequestMapping(value = "art_list", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String getArticleList(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject json = new JSONObject(result);
        String source = request.getParameter("source");
        String tag = request.getParameter("tag");
        String defaultKey = "dKey";
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            if (!StringUtils.isBlank(source)) {
                map.put("source", source);
                defaultKey += source;
            }
            if (!StringUtils.isBlank(tag)) {
                map.put("tag", tag);
                defaultKey += tag;
            }
            map.put("offset", 0);
            map.put("limit", dleyeSwith.getRequestSize());
            List<ArticleInfo> resultList = (List<ArticleInfo>) redisClient.get(defaultKey,
                new TypeReference<List<ArticleInfo>>() {
                });
            if (!CollectionUtils.isEmpty(resultList)) {
                result.put("data", resultList);
                logger.info("use redis key:" + defaultKey);
            } else {
                List<ArticleInfo> articleInfoList = articleInfoManager.getArticleInfosByParams(map);
                if (!CollectionUtils.isEmpty(articleInfoList)) {
                    result.put("data", articleInfoList);
                    redisClient.set(defaultKey, articleInfoList, dleyeSwith.getEffectiveTime());
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
            result.put("msg", "程序小哥跟老板娘跑了");
        }
        return json.toJSONString();
    }

}
