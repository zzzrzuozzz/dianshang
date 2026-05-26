<template>
  <NotificationListPanel
    msg-type="STATION"
    add-route="/ops/station-message/add"
    content-column-label="站内信内容"
    :show-push-status="true"
    :show-push-user="true"
    :station-presets="stationPresets"
    resend-published-label="再推"
    resend-draft-label="推送"
    batch-send-label="推送"
  />
</template>

<script setup>
import NotificationListPanel from '@/components/ops/NotificationListPanel.vue'

const stationPresets = [
  { label: '指定推送', query: { pushMethod: 'assign' } },
  { label: '标签推送', query: { pushMethod: 'tag' } },
]
</script>
