package com.dianshang.admin.content.topic.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TopicDetailVO {

    private String id;
    private Long typeId;
    private String title;
    private String intro;
    private String content;
    private String coverImage;
    private List<String> images;
    private Boolean specifyProducts;
    private List<String> productIds;
    private List<String> memberLevels;
    private List<List<String>> regions;
    private Map<String, List<String>> tags;
    private Integer sort;
    private Integer status;
    private Integer collectCount;
    private Integer readCount;
    private Integer shareCount;
    private List<TopicProductVO> products;
    private List<TopicCommentVO> comments;
}
