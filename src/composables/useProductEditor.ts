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
import type { ProductDetailDto, ProductFormModel, ProductSavePayload } from '@/types/product-form'

const DEFAULT_THUMB =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

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
  }
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
    mainImages: form.mainImages.length ? form.mainImages : [DEFAULT_THUMB],
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
  form.mainImages = detail.mainImages?.length ? detail.mainImages : [DEFAULT_THUMB]
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

  async function loadOptions() {
    const [categories, brands] = await Promise.all([
      fetchCategoryOptions(),
      fetchBrandList({ page: 1, pageSize: 100, status: 'active' }),
    ])
    categoryOptions.value = categories
    brandOptions.value = brands.list.map((b) => ({ label: b.name, value: b.id }))
  }

  async function loadDetail() {
    if (!productId.value) return
    const detail = await fetchProductDetail(productId.value)
    applyDetailToForm(form, detail)
  }

  async function init() {
    pageLoading.value = true
    try {
      await loadOptions()
      if (isEdit.value) {
        await loadDetail()
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

    submitting.value = true
    try {
      const payload = toPayload(form)
      if (isEdit.value) {
        await updateProduct(productId.value, payload)
        ElMessage.success('商品已更新')
      } else {
        const { id } = await createProduct(payload)
        ElMessage.success(`商品已创建，编号 ${id}`)
      }
      router.push('/product/list')
    } finally {
      submitting.value = false
    }
  }

  function mockUpload(field: 'mainImages' | 'whiteImage' | 'video') {
    if (field === 'mainImages' && form.mainImages.length < 5) {
      form.mainImages.push(DEFAULT_THUMB)
    } else if (field === 'whiteImage') {
      form.whiteImage = DEFAULT_THUMB
    } else if (field === 'video') {
      form.video = 'demo.mp4'
    }
    ElMessage.success('上传成功（演示）')
  }

  function removeMainImage(index: number) {
    form.mainImages.splice(index, 1)
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
    isEdit,
    pageTitle,
    productId,
    init,
    nextStep,
    prevStep,
    submit,
    mockUpload,
    removeMainImage,
  }
}
