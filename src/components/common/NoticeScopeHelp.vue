<template>
  <el-alert type="info" :closable="false" show-icon class="notice-scope-help">
    <template #title>{{ title }}</template>
    <template #default>
      <ul class="help-list">
        <li>
          <strong>消息中心（sys_notice）</strong>：管理端铃铛与待办，由系统自动写入（如提现待审、商品待审）或运维手工维护，面向后台账号。
        </li>
        <li>
          <strong>运营推送（ops_notification）</strong>：面向 C 端用户的系统消息 / 短信 / 站内信，在「运营 → 消息推送」中创建与发布，与顶部铃铛数据独立。
        </li>
      </ul>
    </template>
  </el-alert>
</template>

<script setup>
defineProps({
  title: { type: String, default: '两套消息体系说明' },
})
</script>

<style scoped>
.notice-scope-help {
  margin-bottom: 12px;
}
.help-list {
  margin: 8px 0 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}
</style>
