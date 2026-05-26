import { markRaw } from 'vue'
import {
  HomeFilled,
  Goods,
  List,
  Box,
  User,
  Ticket,
  Bell,
  Document,
  DataAnalysis,
  Wallet,
  Setting,
  Lock,
  Location,
  Tools,
} from '@element-plus/icons-vue'

const map = {
  HomeFilled: markRaw(HomeFilled),
  Goods: markRaw(Goods),
  List: markRaw(List),
  Box: markRaw(Box),
  User: markRaw(User),
  Ticket: markRaw(Ticket),
  Bell: markRaw(Bell),
  Document: markRaw(Document),
  DataAnalysis: markRaw(DataAnalysis),
  Wallet: markRaw(Wallet),
  Setting: markRaw(Setting),
  Lock: markRaw(Lock),
  Location: markRaw(Location),
  Tools: markRaw(Tools),
}

export function resolveMenuIcon(key) {
  return map[key] || markRaw(Setting)
}
