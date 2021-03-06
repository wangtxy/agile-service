import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import {
  TabPage as Page, Header, Breadcrumb, Content,
} from '@choerodon/boot';
import { Button, Spin, Icon } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import IsInProgramStore from '@/stores/common/program/IsInProgramStore';
import Version from '../components/VersionComponent/Version';
import Epic from '../components/EpicComponent/Epic';
import Feature from '../components/FeatureComponent/Feature';
import IssueDetail from '../components/issue-detail';
import CreateIssue from '../components/create-issue';
import CreateSprint from '../components/create-sprint';
import SprintList from '../components/sprint-list';
import ShowPlanSprint from '../components/show-plan-sprint';
import './BacklogHome.less';

const createSprintKey = Modal.key();

@observer
class BacklogHome extends Component { 
  componentDidMount() {
    const { BacklogStore } = this.props;
    BacklogStore.refresh();
  }

  refresh = (...args) => {
    const { BacklogStore } = this.props;
    BacklogStore.refresh(...args);
  }

  componentWillUnmount() {
    const { BacklogStore } = this.props;
    BacklogStore.resetData();
    BacklogStore.clearMultiSelected();
    BacklogStore.resetFilter();
  }

  paramConverter = (url) => {
    const reg = /[^?&]([^=&#]+)=([^&#]*)/g;
    const retObj = {};
    url.match(reg).forEach((item) => {
      const [tempKey, paramValue] = item.split('=');
      const paramKey = tempKey[0] !== '&' ? tempKey : tempKey.substring(1);
      Object.assign(retObj, {
        [paramKey]: paramValue,
      });
    });
    return retObj;
  };

  /**
   * 创建冲刺
   */
  handleCreateSprint = () => {
    const { BacklogStore } = this.props;
    const onCreate = (sprint) => {
      BacklogStore.setCreatedSprint(sprint.sprintId);
      this.refresh();
    };
    Modal.open({
      drawer: true,
      style: {
        width: 340,
      },
      key: createSprintKey,
      title: '创建冲刺',
      children: <CreateSprint onCreate={onCreate} />,
    });
  };

  onQuickSearchChange = (onlyMeChecked, onlyStoryChecked, moreChecked) => {
    const { BacklogStore } = this.props;
    BacklogStore.setQuickFilters(onlyMeChecked, onlyStoryChecked, moreChecked);
    BacklogStore.axiosGetSprint()
      .then((res) => {
        BacklogStore.setSprintData(res);
      }).catch((error) => {
      });
  }

  handleClickCBtn = () => {
    const { BacklogStore } = this.props;
    BacklogStore.setNewIssueVisible(true);
  }

  toggleCurrentVisible = (type) => {
    const { BacklogStore } = this.props;
    const currentVisible = BacklogStore.getCurrentVisible;
    if (currentVisible === type) {
      BacklogStore.toggleVisible(null);
    } else {
      BacklogStore.toggleVisible(type);
      if (type === 'feature') {
        BacklogStore.clearMultiSelected();
      }
    }
  };

  render() {
    const { BacklogStore } = this.props;
    const arr = BacklogStore.getSprintData;
    const { isInProgram } = IsInProgramStore;
    return (
      <Fragment>
        <Header title="待办事项">
          <Button
            className="leftBtn"
            funcType="flat"
            onClick={this.handleClickCBtn}
          >
            <Icon type="playlist_add icon" />
            <span>创建问题</span>
          </Button>
          {!isInProgram && (
            <Button className="leftBtn" functyp="flat" onClick={this.handleCreateSprint}>
              <Icon type="playlist_add icon" />
              创建冲刺
            </Button>
          )}
          {isInProgram && arr.length && arr.length > 1
            ? <ShowPlanSprint /> : null
          }
        </Header>
        <Breadcrumb />
        {/* 盖住tab下面的边框 */}
        <div style={{
          width: '100%',
          position: 'absolute',
          background: 'white',
          height: '1px',
          top: '137px',
          left: '109px',
          zIndex: 1,
        }}
        />
        <Content style={{
          padding: 0, paddingTop: 4, display: 'flex', flexDirection: 'column',
        }}
        >
          <div
            className="c7n-backlog"
            style={{
              flex: 1,
              overflow: 'hidden',
            }}
          >
            <div className="c7n-backlog-side">
              <p
                role="none"
                onClick={() => {
                  this.toggleCurrentVisible('version');
                }}
              >
                版本
              </p>
              {!isInProgram && (
                <p
                  style={{
                    marginTop: 12,
                  }}
                  role="none"
                  onClick={() => {
                    this.toggleCurrentVisible('epic');
                  }}
                >
                  史诗
                </p>
              )}
              {isInProgram && (
                <p
                  style={{
                    marginTop: 12,
                  }}
                  role="none"
                  onClick={() => {
                    this.toggleCurrentVisible('feature');
                  }}
                >
                  特性
                </p>
              )}
            </div>
            <Version
              store={BacklogStore}
              refresh={this.refresh}
              visible={BacklogStore.getCurrentVisible}
              issueRefresh={() => {
                this.IssueDetail.refreshIssueDetail();
              }}
            />
            {!isInProgram && (
              <Epic
                refresh={this.refresh}
                visible={BacklogStore.getCurrentVisible}
                issueRefresh={() => {
                  this.IssueDetail.refreshIssueDetail();
                }}
              />
            )}
            <Feature
              refresh={this.refresh}
              isInProgram={isInProgram}
              visible={BacklogStore.getCurrentVisible}
              issueRefresh={() => {
                this.IssueDetail.refreshIssueDetail();
              }}
            />
            <Spin spinning={BacklogStore.getSpinIf}>
              <div className="c7n-backlog-content">
                <SprintList />                
              </div>
            </Spin>
            <CreateIssue />            
            <IssueDetail             
              refresh={() => this.refresh(false)}
              onRef={(ref) => {
                this.IssueDetail = ref;
              }}
            />
          </div>
        </Content>
      </Fragment>
    );
  }
}

export default props => (
  <Page
    service={[
      'agile-service.issue.deleteIssue',
      'agile-service.sprint.queryByProjectId',
      'agile-service.issue.listFeature',
      'agile-service.product-version.queryVersionByProjectId',
      'agile-service.sprint.queryByProjectId',
      'agile-service.priority.queryDefaultByOrganizationId',
      'agile-service.scheme.queryIssueTypesWithStateMachineIdByProjectId',
      'agile-service.quick-filter.listByProjectId',
      'base-service.organization-project.getGroupInfoByEnableProject',
      'agile-service.issue.createIssue',
      'agile-service.field-value.queryPageFieldViewList',
      'agile-service.scheme.queryDefaultByOrganizationId', // /v1/projects/{project_id}/priority/default
      'agile-service.product-version.queryNameByOptions',
      'agile-service.issue-component.queryComponentById',
      'agile-service.scheme.queryByOrganizationIdList',
      'agile-service.sprint.queryNameByOptions',
      'agile-service.issue-label.listIssueLabel',
      'base-service.organization.pagingQueryUsersOnOrganization',
      'agile-service.issue.listEpicSelectData',
      'agile-service.sprint.queryNameByOptions',
      'agile-service.issue-link-type.listIssueLinkType',
      'agile-service.issue.queryIssueByOptionForAgile',
      'agile-service.sprint.queryCompleteMessageBySprintId',
      'agile-service.sprint.completeSprint',
      'base-service.time-zone-work-calendar-project.queryTimeZoneWorkCalendarDetail',
      'agile-service.sprint.querySprintById',
      'agile-service.sprint.startSprint',
      'agile-service.product-version.updateVersion',
    ]}
    className="c7n-backlog-page"
  >
    <BacklogHome {...props} />
  </Page>
);
