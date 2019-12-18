package io.choerodon.agile.app.service.impl;

import java.util.List;
import io.choerodon.agile.api.vo.LabelIssueRelFixVO;
import io.choerodon.agile.infra.dto.LabelIssueRelDTO;
import io.choerodon.agile.app.service.LabelIssueRelService;
import io.choerodon.agile.infra.annotation.DataLog;
import io.choerodon.core.exception.CommonException;
import io.choerodon.agile.infra.mapper.LabelIssueRelMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;


/**
 * 敏捷开发Issue标签关联
 *
 * @author dinghuang123@gmail.com
 * @since 2018-05-14 21:31:22
 */
@Service
public class LabelIssueRelServiceImpl implements LabelIssueRelService {

    private static final String INSERT_ERROR = "error.LabelIssue.insert";

    @Autowired
    private LabelIssueRelMapper labelIssueRelMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @DataLog(type = "labelCreate")
    public LabelIssueRelDTO create(LabelIssueRelDTO labelIssueRelDTO) {
        if (labelIssueRelMapper.insert(labelIssueRelDTO) != 1) {
            throw new CommonException(INSERT_ERROR);
        }
        LabelIssueRelDTO query = new LabelIssueRelDTO();
        query.setIssueId(labelIssueRelDTO.getIssueId());
        query.setLabelId(labelIssueRelDTO.getLabelId());
        return labelIssueRelMapper.selectOne(query);
    }

    @Override
    public int deleteByIssueId(Long issueId) {
        LabelIssueRelDTO labelIssueDO1 = new LabelIssueRelDTO();
        labelIssueDO1.setIssueId(issueId);
        return labelIssueRelMapper.delete(labelIssueDO1);
    }

    @Override
    @DataLog(type = "batchDeleteLabel", single = false)
    public int batchDeleteByIssueId(Long issueId) {
        LabelIssueRelDTO delete = new LabelIssueRelDTO();
        delete.setIssueId(issueId);
        return labelIssueRelMapper.delete(delete);
    }

    @Override
    @DataLog(type = "labelDelete")
    public int delete(LabelIssueRelDTO labelIssueRelDTO) {
        return labelIssueRelMapper.delete(labelIssueRelDTO);
    }

    @Override
    public List<LabelIssueRelFixVO> queryProjectLabelIssueRel(Long projectId) {
        List<LabelIssueRelDTO> labelIssueRelDTOS = labelIssueRelMapper.queryByIssueIds(projectId);
        List<LabelIssueRelFixVO> labelIssueRelFixVOS = modelMapper.map(labelIssueRelDTOS, new TypeToken<List<LabelIssueRelFixVO>>() {
        }.getType());
        return labelIssueRelFixVOS;
    }
}