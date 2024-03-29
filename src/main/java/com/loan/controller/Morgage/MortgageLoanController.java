package com.loan.controller.Morgage;

import com.loan.entity.MortgageRecord;
import com.loan.service.MortgageRecordService;
import com.loan.util.Constant;
import com.loan.util.DataReturn;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(value = "抵押贷款放贷模块", tags = "抵押贷款放贷模块")
@RestController
@RequestMapping("/loan")
public class MortgageLoanController {
    @Autowired
    private MortgageRecordService mortgageRecordService;

    @Autowired
    private TaskService taskService;

    @ApiOperation(value = "确定放款状态", notes = "确定放款状态")
    @ResponseBody
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public DataReturn<String> confirmLoan(@RequestParam(value = "taskId", defaultValue = "") String taskId){
        if("".equals(taskId)){
            return new DataReturn<>(Constant.RESULT_ERROR, "输入参数不合法" , "");
        }
        MortgageRecord mortgageRecord = mortgageRecordService.findOneById(taskService.getVariable(taskId, Constant.LOANID).toString());
        if(null == mortgageRecord){
            return new DataReturn<>(Constant.RESULT_ERROR, "贷款记录不存在", "");
        }
        try {
            //修改贷款记录
            mortgageRecord.setRecord_state(Constant.LOANRECORD_COMPLETE);
            mortgageRecord = mortgageRecordService.save(mortgageRecord);
            if(null == mortgageRecord){
                return new DataReturn<>(Constant.RESULT_ERROR, "确定放款状态失败", "");
            }
            Map<String, Object> map = new HashMap<>();
            if(mortgageRecord.isCharge_skip()){
                map.put("skipCharge", Constant.CHARGE_YES);
            }else {
                map.put("skipCharge", Constant.CHARGE_NO);
            }
            taskService.complete(taskId);
        }catch (Exception e){
            return new DataReturn<>(Constant.RESULT_ERROR, "确定放款状态失败", "");
        }
        return new DataReturn<>(Constant.RESULT_OK, "确定放款状态成功", mortgageRecord.getId());
    }
}
