package com.mumomu.exquizme.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class ProblemSaveDto {
    private Long problemsetId;
    private String dtype; //"MultipleChoiceProblem, OXProblem, SubjectiveProblem
    private Integer idx; //문제 번호
    private String title;
    private String description;
    private Integer timelimit;
    private Integer score;
    private String picture;

    private String answer;

    public ProblemSaveDto(Long problemsetId, String dtype, Integer idx, String title, String description, Integer timelimit, Integer score) {
        this.problemsetId = problemsetId;
        this.dtype = dtype;
        this.idx = idx;
        this.title = title;
        this.description = description;
        this.timelimit = timelimit;
        this.score = score;
    }
}
