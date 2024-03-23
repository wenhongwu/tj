package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.service.IInteractionQuestionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 互动提问的问题表 前端控制器
 * </p>
 *
 * @author whw
 * @since 2024-03-22
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class InteractionQuestionController {

    private final IInteractionQuestionService questionService;

    @ApiOperation("新增提问")
    @PostMapping
    public void saveQuestion(@Valid @RequestBody QuestionFormDTO questionFormDTO) {
        questionService.saveQuestion(questionFormDTO);
    }

    @ApiOperation("修改提问")
    @PutMapping("/{id}")
    public void updateQuestion(
            @ApiParam("要修改的问题id") @PathVariable("id") Long id,
            @RequestBody QuestionFormDTO questionFormDTO) {

         questionService.updateQuestion(id, questionFormDTO);
    }

    @ApiOperation("分页查询互动问题")
    @GetMapping("page")
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query){
        return questionService.queryQuestionPage(query);
    }

    @ApiOperation("根据id查询问题详情")
    @GetMapping("/{id}")
    public QuestionVO queryQuestionById(@ApiParam(value = "问题id", example = "1") @PathVariable("id") Long id){
        return questionService.queryQuestionById(id);
    }

    @ApiOperation("根据id删除当前用户问题")
    @DeleteMapping("/{id}")
    public void deleteQuestion(
            @ApiParam(value = "问题id", example = "1") @PathVariable("id") Long id
    ){
        questionService.deleteById(id);
    }

}
