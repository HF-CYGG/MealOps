export const FALLBACK_DISH_IMAGE =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 420">
      <defs>
        <linearGradient id="bg" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0%" stop-color="#f6f1ea"/>
          <stop offset="100%" stop-color="#e7d0b7"/>
        </linearGradient>
      </defs>
      <rect width="640" height="420" rx="44" fill="url(#bg)"/>
      <circle cx="490" cy="82" r="84" fill="#d4a373" opacity=".18"/>
      <circle cx="134" cy="352" r="110" fill="#1c1c1e" opacity=".08"/>
      <g fill="none" stroke="#1c1c1e" stroke-linecap="round" stroke-linejoin="round" stroke-width="14" opacity=".72">
        <path d="M214 250h212"/>
        <path d="M244 250c10 54 44 82 76 82s66-28 76-82"/>
        <path d="M262 196c-24-28-12-56 12-76"/>
        <path d="M326 190c-22-28-12-54 12-72"/>
        <path d="M390 196c-22-28-12-54 12-72"/>
      </g>
      <text x="320" y="360" text-anchor="middle" fill="#1c1c1e" opacity=".58"
        font-family="Arial, sans-serif" font-size="28" font-weight="700">MealOps</text>
    </svg>
  `)

const ABSOLUTE_URL_PATTERN = /^(https?:)?\/\//i

export const resolveImageUrl = (value, fallback = FALLBACK_DISH_IMAGE) => {
  if (!value || typeof value !== 'string') {
    return fallback
  }

  const raw = value.trim()
  if (!raw) {
    return fallback
  }

  if (ABSOLUTE_URL_PATTERN.test(raw) || raw.startsWith('data:') || raw.startsWith('blob:')) {
    return raw
  }

  if (raw.startsWith('/api/common/download')) {
    return raw
  }

  if (raw.startsWith('/api/images/')) {
    return raw
  }

  if (raw.startsWith('/common/download')) {
    return `/api${raw}`
  }

  if (raw.startsWith('common/download')) {
    return `/api/${raw}`
  }

  if (raw.startsWith('/images/')) {
    return `/api${raw}`
  }

  if (raw.startsWith('images/')) {
    return `/api/${raw}`
  }

  return raw.startsWith('/') ? raw : `/${raw}`
}

export const applyImageFallback = (event) => {
  if (event?.target && event.target.src !== FALLBACK_DISH_IMAGE) {
    event.target.src = FALLBACK_DISH_IMAGE
  }
}
