import { markRaw, type Component } from 'vue'
import {
  Document,
  User,
  CreditCard,
  Money,
  Wallet,
  Goods,
  List,
  DataAnalysis,
  Promotion,
  Setting,
} from '@element-plus/icons-vue'

const iconMap: Record<string, Component> = {
  Document: markRaw(Document),
  User: markRaw(User),
  CreditCard: markRaw(CreditCard),
  Money: markRaw(Money),
  Wallet: markRaw(Wallet),
  Goods: markRaw(Goods),
  List: markRaw(List),
  DataAnalysis: markRaw(DataAnalysis),
  Promotion: markRaw(Promotion),
  Setting: markRaw(Setting),
}

export function resolveDashboardIcon(iconKey: string) {
  return iconMap[iconKey] || markRaw(Document)
}
