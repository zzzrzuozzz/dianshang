import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  createProduct,
  fetchBrandList,
  fetchCategoryOptions,
  fetchProductDetail,
  updateProduct,
} from '@/api/product'
import { fetchInventoryList, updateInventory } from '@/api/inventory'
import { fetchExpressTemplateList } from '@/api/order'
import type {
  ProductDetailDto,
  ProductFormModel,
  ProductSavePayload,
  ProductSkuRow,
} from '@/types/product-form'

export function createEmptyProductForm(): ProductFormModel {
  return {
    category: '',
    name: '',
    subtitle: '',
    brand: '',
    intro: '',
    shipping: '',
    deliveryRegions: [],
    sku: '',
    price: '',
    marketPrice: '',
    stock: '',
    stockWarning: '',
    unit: '',
    weight: '',
    preSale: false,
    onSale: false,
    recommend: [],
    services: [],
    tags: '',
    mainImages: [],
    whiteImage: '',
    video: '',
    detail: '',
    skuRows: [],
  }
}

function createPrimarySkuRow(form: ProductFormModel, productNo = ''): ProductSkuRow {
  return {
    skuId: productNo ? `P-${productNo}` : '',
    skuName: form.subtitle || form.name || '默认规格',
    skuCode: form.sku || '',
    stock: form.stock || '0',
    stockWarning: form.stockWarning || '0',
    isPrimary: true,
  }
}

function nextExtensionSkuId(): string {
  return `E-${Date.now().toString(36).slice(-6)}`
}

function toPayload(form: ProductFormModel): ProductSavePayload {
  return {
    category: form.category,
    name: form.name.trim(),
    subtitle: form.subtitle || undefined,
    brand: form.brand,
    intro: form.intro.trim(),
    shipping: form.shipping || undefined,
    deliveryRegions:
      form.deliveryRegions?.length > 0 ? form.deliveryRegions : undefined,
    sku: form.sku.trim() || undefined,
    price: Number(form.price),
    marketPrice: form.marketPrice ? Number(form.marketPrice) : undefined,
    stock: Number(form.stock),
    stockWarning: form.stockWarning ? Number(form.stockWarning) : 0,
    unit: form.unit || undefined,
    weight: form.weight || undefined,
    preSale: form.preSale,
    onSale: form.onSale,
    recommend: form.recommend,
    services: form.services,
    tags: form.tags || undefined,
    mainImages: form.mainImages,
    whiteImage: form.whiteImage || undefined,
    video: form.video || undefined,
    detail: form.detail || undefined,
  }
}

function applyDetailToForm(form: ProductFormModel, detail: ProductDetailDto) {
  form.category = detail.category
  form.name = detail.name
  form.subtitle = detail.subtitle ?? ''
  form.brand = detail.brand
  form.intro = detail.intro
  form.shipping = detail.shipping ?? ''
  form.deliveryRegions = detail.deliveryRegions?.length ? [...detail.deliveryRegions] : []
  form.sku = detail.sku ?? ''
  form.price = String(detail.price ?? '')
  form.marketPrice = detail.marketPrice != null ? String(detail.marketPrice) : ''
  form.stock = String(detail.stock ?? '')
  form.stockWarning = detail.stockWarning != null ? String(detail.stockWarning) : ''
  form.unit = detail.unit ?? ''
  form.weight = detail.weight ?? ''
  form.preSale = Boolean(detail.preSale)
  form.onSale = Boolean(detail.onSale)
  form.recommend = detail.recommend ?? []
  form.services = detail.services ?? []
  form.tags = detail.tags ?? ''
  form.mainImages = detail.mainImages?.length ? [...detail.mainImages] : []
  form.whiteImage = detail.whiteImage ?? ''
  form.video = detail.video ?? ''
  form.detail = detail.detail ?? ''
}

export function useProductEditor() {
  const route = useRoute()
  const router = useRouter()

  const formRef = ref<FormInstance>()
  const form = reactive<ProductFormModel>(createEmptyProductForm())
  const currentStep = ref(0)
  const pageLoading = ref(false)
  const submitting = ref(false)
  const categoryOptions = ref<{ label: string; value: string }[]>([])
  const brandOptions = ref<{ label: string; value: string }[]>([])
  const shippingOptions = ref<{ label: string; value: string }[]>([])

  const productId = computed(() => String(route.query.id || ''))
  const isEdit = computed(() => Boolean(productId.value))
  const pageTitle = computed(() => (isEdit.value ? '编辑商品' : '添加商品'))

  const numberPattern = /^\d+(\.\d{1,2})?$/
  const intPattern = /^\d+$/

  const rules: FormRules<ProductFormModel> = {
    category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
    name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    brand: [{ required: true, message: '请选择品牌', trigger: 'change' }],
    intro: [{ required: true, message: '请输入商品介绍', trigger: 'blur' }],
    price: [
      { required: true, message: '请输入售价', trigger: 'blur' },
      { pattern: numberPattern, message: '售价格式不正确', trigger: 'blur' },
    ],
    stock: [
      { required: true, message: '请输入库存', trigger: 'blur' },
      { pattern: intPattern, message: '库存须为非负整数', trigger: 'blur' },
    ],
  }

  function ensureSkuRows(productNo = '') {
    if (form.skuRows.length === 0) {
      form.skuRows.push(createPrimarySkuRow(form, productNo))
      return
    }
    const primary = form.skuRows.find((r) => r.isPrimary)
    if (primary) {
      primary.skuName = form.subtitle || form.name || primary.skuName
      primary.skuCode = form.sku
      primary.stock = form.stock
      primary.stockWarning = form.stockWarning || '0'
      if (productNo) primary.skuId = `P-${productNo}`
    } else {
      form.skuRows.unshift(createPrimarySkuRow(form, productNo))
    }
  }

  function syncFormFromPrimaryRow() {
    const primary = form.skuRows.find((r) => r.isPrimary) ?? form.skuRows[0]
    if (!primary) return
    form.sku = primary.skuCode
    form.stock = primary.stock
    form.stockWarning = primary.stockWarning
  }

  async function loadInventorySkus(goodsId: string) {
    const page = await fetchInventoryList({ keyword: goodsId, page: 1, pageSize: 20 })
    const item = page.list.find((i) => i.goodsId === goodsId) ?? page.list[0]
    if (!item?.skus?.length) {
      ensureSkuRows(goodsId)
      return
    }
    form.skuRows = item.skus.map((s) => ({
      skuId: s.skuId,
      skuName: s.skuName,
      skuCode: s.skuCode || '',
      stock: String(s.actualStock ?? 0),
      stockWarning: String(s.warningStock ?? 0),
      isPrimary: s.skuId.startsWith('P-'),
    }))
  }

  async function syncInventorySkus(goodsId: string) {
    ensureSkuRows(goodsId)
    const skus = form.skuRows.map((row) => ({
      skuName: row.skuName.trim() || (row.isPrimary ? form.subtitle || form.name : '扩展规格'),
      skuId: row.isPrimary ? `P-${goodsId}` : row.skuId || nextExtensionSkuId(),
      skuCode: row.skuCode.trim() || (row.isPrimary ? form.sku : `SKU-${goodsId}`),
      actualStock: Number(row.stock) || 0,
      warningStock: Number(row.stockWarning) || 0,
      warehouseCode: 'WH-001',
    }))
    await updateInventory({ goodsId, skus })
  }

  function validateSkuRows(): boolean {
    ensureSkuRows(productId.value)
    if (form.skuRows.length === 0) {
      ElMessage.warning('请至少配置一个 SKU 规格')
      return false
    }
    for (const row of form.skuRows) {
      if (!row.skuName.trim()) {
        ElMessage.warning('请填写规格名称')
        return false
      }
      if (!intPattern.test(row.stock)) {
        ElMessage.warning(`规格「${row.skuName}」库存须为非负整数`)
        return false
      }
      if (row.stockWarning && !intPattern.test(row.stockWarning)) {
        ElMessage.warning(`规格「${row.skuName}」预警值须为非负整数`)
        return false
      }
    }
    return true
  }

  function addSkuRow() {
    form.skuRows.push({
      skuId: nextExtensionSkuId(),
      skuName: '',
      skuCode: '',
      stock: '0',
      stockWarning: '0',
      isPrimary: false,
    })
  }

  function removeSkuRow(index: number) {
    const row = form.skuRows[index]
    if (row?.isPrimary) {
      ElMessage.warning('主规格不可删除')
      return
    }
    form.skuRows.splice(index, 1)
  }

  async function loadOptions() {
    const [categories, brands, templates] = await Promise.all([
      fetchCategoryOptions(),
      fetchBrandList({ page: 1, pageSize: 100, status: 'active' }),
      fetchExpressTemplateList({ page: 1, pageSize: 100 }),
    ])
    categoryOptions.value = categories
    brandOptions.value = brands.list.map((b) => ({ label: b.name, value: b.id }))
    shippingOptions.value = (templates.list || [])
      .filter((t) => t.visible !== false)
      .map((t) => ({
        label: `${t.templateName}（${t.expressCompany}）`,
        value: t.id,
      }))
  }

  async function loadDetail() {
    if (!productId.value) return
    const detail = await fetchProductDetail(productId.value)
    applyDetailToForm(form, detail)
    await loadInventorySkus(productId.value)
  }

  async function init() {
    pageLoading.value = true
    try {
      await loadOptions()
      if (isEdit.value) {
        await loadDetail()
      } else {
        ensureSkuRows()
      }
    } finally {
      pageLoading.value = false
    }
  }

  async function validateStep(): Promise<boolean> {
    if (!formRef.value) return false
    return formRef.value.validate().catch(() => false)
  }

  async function nextStep() {
    if (currentStep.value === 0) {
      const valid = await validateStep()
      if (!valid) return
      if (form.mainImages.length === 0) {
        ElMessage.warning('请至少上传一张商品主图')
        return
      }
      ensureSkuRows(productId.value)
    }
    if (currentStep.value === 1) {
      if (!validateSkuRows()) return
      syncFormFromPrimaryRow()
    }
    if (currentStep.value < 2) {
      currentStep.value += 1
    }
  }

  function prevStep() {
    if (currentStep.value > 0) {
      currentStep.value -= 1
    }
  }

  async function submit() {
    const valid = await validateStep()
    if (!valid) {
      currentStep.value = 0
      return
    }
    if (form.mainImages.length === 0) {
      ElMessage.warning('请至少上传一张商品主图')
      currentStep.value = 0
      return
    }
    if (!validateSkuRows()) {
      currentStep.value = 1
      return
    }
    syncFormFromPrimaryRow()

    submitting.value = true
    try {
      const payload = toPayload(form)
      let goodsId = productId.value
      if (isEdit.value) {
        await updateProduct(productId.value, payload)
        ElMessage.success('商品已更新')
      } else {
        const { id } = await createProduct(payload)
        goodsId = id
        ElMessage.success(`商品已创建，编号 ${id}`)
      }
      await syncInventorySkus(goodsId)
      router.push('/product/list')
    } finally {
      submitting.value = false
    }
  }

  return {
    formRef,
    form,
    rules,
    currentStep,
    pageLoading,
    submitting,
    categoryOptions,
    brandOptions,
    shippingOptions,
    isEdit,
    pageTitle,
    productId,
    init,
    nextStep,
    prevStep,
    submit,
    addSkuRow,
    removeSkuRow,
  }
}
