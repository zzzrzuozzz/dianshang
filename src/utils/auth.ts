const TOKEN_KEY = 'admin_token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string, remember = true): void {
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token)
    sessionStorage.removeItem(TOKEN_KEY)
  } else {
    sessionStorage.setItem(TOKEN_KEY, token)
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(TOKEN_KEY)
}
