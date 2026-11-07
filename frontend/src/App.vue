<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const medicines = ref([])
const orders = ref([])
const cart = ref({})
const activeCategory = ref('Tất cả')
const search = ref('')
const checkoutOpen = ref(false)
const historyOpen = ref(false)
const toast = ref('')
const prescriptionConfirmed = ref(false)
const loading = ref(true)
const submitting = ref(false)
const loadError = ref('')
const customer = reactive({ name: '', phone: '', address: '' })
let toastTimer
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''

async function apiRequest(url, options) {
  const response = await fetch(`${apiBaseUrl}${url}`, options)
  let body = {}
  try {
    body = await response.json()
  } catch (error) {
    if (response.ok) throw new Error('Phản hồi từ máy chủ không hợp lệ.')
  }
  if (!response.ok) {
    throw new Error(body.message || `Máy chủ trả về lỗi ${response.status}.`)
  }
  return body
}

async function loadData() {
  loading.value = true
  loadError.value = ''
  try {
    const [medicineList, orderList] = await Promise.all([
      apiRequest('/api/medicines'),
      apiRequest('/api/orders'),
    ])
    medicines.value = medicineList
    orders.value = orderList
  } catch (error) {
    loadError.value = error.message || 'Không thể tải dữ liệu từ máy chủ.'
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const categories = computed(() => [
  'Tất cả',
  ...new Set(medicines.value.map((medicine) => medicine.category)),
])

const filteredMedicines = computed(() => {
  const query = search.value.trim().toLocaleLowerCase('vi')
  return medicines.value.filter((medicine) => {
    const matchesCategory =
      activeCategory.value === 'Tất cả' || medicine.category === activeCategory.value
    const matchesSearch =
      !query ||
      medicine.name.toLocaleLowerCase('vi').includes(query) ||
      medicine.category.toLocaleLowerCase('vi').includes(query)
    return matchesCategory && matchesSearch
  })
})

const cartItems = computed(() =>
  Object.entries(cart.value)
    .map(([id, quantity]) => {
      const medicine = medicines.value.find((item) => item.id === Number(id))
      return medicine ? { medicine, quantity } : null
    })
    .filter(Boolean),
)

const cartCount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.quantity, 0),
)
const subtotal = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.medicine.price * item.quantity, 0),
)
const hasPrescriptionMedicine = computed(() =>
  cartItems.value.some((item) => item.medicine.prescription),
)

function formatPrice(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(value)
}

function notify(message) {
  toast.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => (toast.value = ''), 2600)
}

function addToCart(medicine) {
  const quantity = cart.value[medicine.id] || 0
  if (new Date(`${medicine.expiry}T23:59:59`) < new Date()) {
    notify('Sản phẩm đã hết hạn, không thể đặt mua.')
    return
  }
  if (quantity >= medicine.stock) {
    notify('Số lượng đặt đã đạt mức tồn kho.')
    return
  }
  cart.value = { ...cart.value, [medicine.id]: quantity + 1 }
  notify(`Đã thêm ${medicine.name} vào giỏ`)
}

function changeQuantity(medicine, change) {
  const next = (cart.value[medicine.id] || 0) + change
  if (next <= 0) {
    const updated = { ...cart.value }
    delete updated[medicine.id]
    cart.value = updated
    return
  }
  if (next > medicine.stock) {
    notify('Số lượng đặt đã đạt mức tồn kho.')
    return
  }
  cart.value = { ...cart.value, [medicine.id]: next }
}

function openCheckout() {
  if (!cartCount.value) {
    notify('Giỏ hàng của bạn đang trống.')
    return
  }
  prescriptionConfirmed.value = false
  checkoutOpen.value = true
}

async function placeOrder() {
  if (submitting.value || (hasPrescriptionMedicine.value && !prescriptionConfirmed.value)) return
  submitting.value = true
  try {
    const order = await apiRequest('/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customer: { ...customer },
        items: cartItems.value.map(({ medicine, quantity }) => ({
          medicineId: medicine.id,
          quantity,
        })),
        prescriptionConfirmed: prescriptionConfirmed.value,
      }),
    })
    orders.value = [order, ...orders.value]
    cart.value = {}
    customer.name = ''
    customer.phone = ''
    customer.address = ''
    checkoutOpen.value = false
    historyOpen.value = true
    await loadData()
    notify(`Đặt hàng thành công · Mã đơn ${order.id}`)
  } catch (error) {
    notify(error.message || 'Không thể tạo đơn hàng. Vui lòng thử lại.')
  } finally {
    submitting.value = false
  }
}

function formatDate(value) {
  return new Intl.DateTimeFormat('vi-VN', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}
</script>

<template>
  <div class="page-shell">
    <header class="topbar">
      <a class="brand" href="#" aria-label="Nhà thuốc Bình An">
        <span class="brand-mark">+</span>
        <span class="brand-name">bình an<span>.</span></span>
      </a>
      <nav class="main-nav" aria-label="Điều hướng chính">
        <a class="nav-link active" href="#san-pham">Sản phẩm</a>
        <button class="nav-link" type="button" @click="historyOpen = !historyOpen">
          Đơn hàng
        </button>
        <a class="nav-link" href="#lien-he">Liên hệ</a>
      </nav>
      <button class="cart-shortcut" type="button" @click="openCheckout">
        <span class="cart-icon">▱</span>
        <span>Giỏ hàng</span>
        <span v-if="cartCount" class="cart-badge">{{ cartCount }}</span>
      </button>
    </header>

    <main>
      <div v-if="loadError" class="load-error" role="alert">
        <span>{{ loadError }}</span>
        <button type="button" @click="loadData">Thử tải lại</button>
      </div>
      <section class="hero">
        <div class="hero-copy">
          <div class="eyebrow"><span class="live-dot"></span> NHÀ THUỐC GẦN BẠN</div>
          <h1>Chăm sóc sức khỏe,<br /><span>giao tận nhà.</span></h1>
          <p>
            Thuốc chính hãng, tư vấn tận tâm. Đặt hàng nhanh chóng, giao an toàn đến cửa nhà bạn.
          </p>
          <a class="primary-button hero-button" href="#san-pham">
            Khám phá sản phẩm <span aria-hidden="true">→</span>
          </a>
          <div class="hero-trust">
            <span class="trust-avatars" aria-hidden="true"><i>✦</i><i>♡</i><i>+</i></span>
            <span>Được tin dùng bởi <strong>2.000+</strong> gia đình</span>
          </div>
        </div>
        <div class="hero-art" aria-label="Minh họa chăm sóc sức khỏe">
          <div class="art-circle circle-back"></div>
          <div class="art-circle circle-front"></div>
          <div class="plant plant-one">⌁</div>
          <div class="medicine-bottle"><span>+</span><small>CARE<br />DAILY</small></div>
          <div class="medicine-box"><b>+</b><small>HEALTH<br />& WELLNESS</small></div>
          <div class="art-note"><span>✓</span> Bình an mỗi ngày</div>
          <div class="art-sparkle sparkle-one">✳</div>
          <div class="art-sparkle sparkle-two">✦</div>
        </div>
        <div class="hero-footnote"><span>✦</span> Sức khỏe của bạn là ưu tiên của chúng tôi</div>
      </section>

      <section class="benefits" aria-label="Lợi ích">
        <div class="benefit">
          <span class="benefit-icon green-icon">⌂</span>
          <div><strong>Giao hàng tận nhà</strong><small>Trong 2 giờ nội thành</small></div>
        </div>
        <div class="benefit">
          <span class="benefit-icon peach-icon">✓</span>
          <div><strong>Thuốc chính hãng</strong><small>Nguồn gốc rõ ràng</small></div>
        </div>
        <div class="benefit">
          <span class="benefit-icon lilac-icon">♡</span>
          <div><strong>Dược sĩ tận tâm</strong><small>Luôn sẵn sàng hỗ trợ</small></div>
        </div>
      </section>

      <section id="san-pham" class="shop-section">
        <div class="section-heading">
          <div>
            <div class="eyebrow section-eyebrow">LỰA CHỌN CHO BẠN</div>
            <h2>Sản phẩm nổi bật</h2>
            <p class="section-subtitle">Chăm sóc sức khỏe mỗi ngày, bắt đầu từ những điều nhỏ nhất.</p>
          </div>
          <label class="search-box">
            <span aria-hidden="true">⌕</span>
            <input v-model="search" type="search" placeholder="Tìm tên thuốc..." />
            <kbd>⌘ K</kbd>
          </label>
        </div>

        <div class="shop-layout">
          <div class="catalog">
            <div class="category-list" aria-label="Lọc theo danh mục">
              <button
                v-for="category in categories"
                :key="category"
                class="category-chip"
                :class="{ selected: activeCategory === category }"
                type="button"
                @click="activeCategory = category"
              >
                {{ category }}
              </button>
            </div>

            <div v-if="loading" class="empty-state">
              <strong>Đang tải sản phẩm...</strong>
            </div>
            <div v-else-if="filteredMedicines.length" class="product-grid">
              <article v-for="medicine in filteredMedicines" :key="medicine.id" class="product-card">
                <div class="product-visual" :class="medicine.tone">
                  <span v-if="medicine.prescription" class="rx-badge">CẦN ĐƠN</span>
                  <span class="product-symbol">{{ medicine.icon }}</span>
                  <span class="product-caption">{{ medicine.category }}</span>
                </div>
                <div class="product-info">
                  <span class="product-category">{{ medicine.category }}</span>
                  <h3>{{ medicine.name }}</h3>
                  <p>{{ medicine.description }}</p>
                  <div class="product-bottom">
                    <div>
                      <strong>{{ formatPrice(medicine.price) }}</strong>
                      <small>{{ medicine.stock > 0 ? `Còn ${medicine.stock} sản phẩm` : 'Tạm hết hàng' }}</small>
                    </div>
                    <button
                      class="add-button"
                      type="button"
                      :disabled="medicine.stock < 1"
                      :aria-label="`Thêm ${medicine.name} vào giỏ`"
                      @click="addToCart(medicine)"
                    >+</button>
                  </div>
                </div>
              </article>
            </div>
            <div v-else class="empty-state">
              <span>⌕</span>
              <strong>Chưa tìm thấy sản phẩm</strong>
              <p>Thử tìm bằng tên thuốc hoặc chọn danh mục khác nhé.</p>
            </div>
          </div>

          <aside class="cart-panel">
            <div class="cart-panel-heading">
              <div><span class="eyebrow section-eyebrow">ĐƠN CỦA BẠN</span><h3>Giỏ hàng</h3></div>
              <span class="cart-count">{{ cartCount }} món</span>
            </div>
            <div v-if="!cartItems.length" class="empty-cart">
              <span class="empty-cart-icon">▱</span>
              <strong>Giỏ hàng đang trống</strong>
              <p>Chọn sản phẩm bạn cần, chúng tôi sẽ chuẩn bị đơn hàng ngay.</p>
            </div>
            <div v-else class="cart-lines">
              <div v-for="{ medicine, quantity } in cartItems" :key="medicine.id" class="cart-line">
                <div class="cart-line-copy">
                  <strong>{{ medicine.name }}</strong>
                  <small>{{ formatPrice(medicine.price) }}</small>
                  <span v-if="medicine.prescription" class="cart-rx">Thuốc kê đơn</span>
                </div>
                <div class="quantity-control">
                  <button type="button" :aria-label="`Giảm ${medicine.name}`" @click="changeQuantity(medicine, -1)">−</button>
                  <span>{{ quantity }}</span>
                  <button type="button" :aria-label="`Tăng ${medicine.name}`" @click="changeQuantity(medicine, 1)">+</button>
                </div>
              </div>
            </div>
            <div class="cart-summary">
              <div><span>Tạm tính</span><strong>{{ formatPrice(subtotal) }}</strong></div>
              <div><span>Phí giao hàng</span><strong class="free-shipping">Miễn phí</strong></div>
              <div class="total-row"><span>Tổng cộng</span><strong>{{ formatPrice(subtotal) }}</strong></div>
              <button class="primary-button checkout-button" type="button" :disabled="!cartCount" @click="openCheckout">
                Tiến hành đặt hàng <span aria-hidden="true">→</span>
              </button>
              <p class="secure-note"><span>♧</span> Thông tin của bạn được bảo mật</p>
            </div>
          </aside>
        </div>
      </section>

      <section v-if="historyOpen" class="history-section">
        <div class="history-heading">
          <div><div class="eyebrow section-eyebrow">THEO DÕI MUA HÀNG</div><h2>Đơn hàng gần đây</h2></div>
          <button class="close-history" type="button" @click="historyOpen = false">Ẩn lịch sử ×</button>
        </div>
        <div v-if="!orders.length" class="history-empty">Bạn chưa có đơn hàng nào.</div>
        <article v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-top">
            <strong>Mã đơn {{ order.id }}</strong>
            <span>{{ formatDate(order.createdAt) }}</span>
            <b>{{ order.status }}</b>
          </div>
          <p>{{ order.items.map((item) => `${item.name} × ${item.quantity}`).join(' · ') }}</p>
          <div class="order-bottom"><span>Giao hàng tận nhà</span><strong>{{ formatPrice(order.total) }}</strong></div>
        </article>
      </section>
    </main>

    <footer id="lien-he" class="footer">
      <a class="brand footer-brand" href="#"><span class="brand-mark">+</span><span class="brand-name">bình an<span>.</span></span></a>
      <p>Đồng hành cùng sức khỏe gia đình bạn.</p>
      <span>☎ 1900 1234 <i>·</i> Mỗi ngày, 7:00 – 22:00</span>
    </footer>

    <div v-if="checkoutOpen" class="modal-backdrop" @click.self="checkoutOpen = false">
      <section class="checkout-modal" role="dialog" aria-modal="true" aria-labelledby="checkout-title">
        <button class="modal-close" type="button" aria-label="Đóng" @click="checkoutOpen = false">×</button>
        <div class="eyebrow section-eyebrow">GIAO HÀNG TẬN NHÀ</div>
        <h2 id="checkout-title">Thông tin nhận hàng</h2>
        <p class="modal-intro">Để lại thông tin, nhà thuốc sẽ liên hệ xác nhận đơn hàng.</p>
        <form @submit.prevent="placeOrder">
          <label class="form-field">Họ và tên
            <input v-model.trim="customer.name" required maxlength="80" autocomplete="name" placeholder="Tên người nhận" />
          </label>
          <label class="form-field">Số điện thoại
            <input v-model.trim="customer.phone" required maxlength="20" type="tel" autocomplete="tel" placeholder="Ví dụ: 090 123 4567" />
          </label>
          <label class="form-field">Địa chỉ giao hàng
            <textarea v-model.trim="customer.address" required maxlength="240" rows="2" autocomplete="street-address" placeholder="Số nhà, đường, phường/xã, quận/huyện"></textarea>
          </label>
          <div class="checkout-total"><span>Tổng thanh toán</span><strong>{{ formatPrice(subtotal) }}</strong></div>
          <label v-if="hasPrescriptionMedicine" class="prescription-confirm">
            <input v-model="prescriptionConfirmed" type="checkbox" required />
            <span>Tôi xác nhận khách hàng đã cung cấp đơn thuốc hợp lệ cho thuốc kê đơn.</span>
          </label>
          <button class="primary-button checkout-button" type="submit"
            :disabled="submitting || (hasPrescriptionMedicine && !prescriptionConfirmed)">
            {{ submitting ? 'Đang gửi đơn hàng...' : 'Xác nhận đặt hàng' }}
            <span v-if="!submitting" aria-hidden="true">→</span>
          </button>
          <p class="medical-note">Thông tin sản phẩm không thay thế tư vấn của bác sĩ hoặc dược sĩ.</p>
        </form>
      </section>
    </div>

    <Transition name="toast">
      <div v-if="toast" class="toast-message" role="status"><span>✓</span>{{ toast }}</div>
    </Transition>
  </div>
</template>
