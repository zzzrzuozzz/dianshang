<template>
  <NotificationListPanel
    msg-type="STATION"
    add-route="/ops/station-message/add"
    content-column-label="站内信内容"
    :show-push-status="true"
    :show-push-user="true"
    :action-width="200"
    resend-published-label="再推"
    resend-draft-label="推送"
  />
</template>

<script setup>
import NotificationListPanel from '@/components/ops/NotificationListPanel.vue'
</script>
