'use strict';
const password = document.getElementById('password');
const toggle = document.getElementById('toggle-password');
const params = new URLSearchParams(location.search);
// 로그인 후 돌아갈 페이지. 다른 사이트로 보내지 않도록 이 사이트의 .html 페이지만 허용합니다.
const nextPage = /^[a-z0-9-]+\.html([?#].*)?$/i.test(params.get('next') || '') ? params.get('next') : 'index.html';
const loginRequired = document.getElementById('login-required');
const isFavorites = nextPage.startsWith('favorites.html') || nextPage.includes('#favorites');
loginRequired.hidden = !params.has('next');
loginRequired.textContent = isFavorites
  ? '단골 목록은 로그인 후 사용할 수 있어요. 로그인하면 마음에 드는 식당을 모아볼 수 있어요.'
  : nextPage.startsWith('mypage.html')
    ? '마이페이지를 이용하려면 로그인이 필요해요.'
    : nextPage.startsWith('results.html') && new URLSearchParams(nextPage.split('?')[1]).has('query')
      ? 'AI에게 물어보기는 로그인 후 사용할 수 있어요.'
      : '단골에 추가하려면 로그인이 필요해요.';
toggle.addEventListener('click', () => {
  const visible = password.type === 'password';
  password.type = visible ? 'text' : 'password';
  toggle.setAttribute('aria-pressed', String(visible));
  toggle.setAttribute('aria-label', visible ? '비밀번호 숨기기' : '비밀번호 보기');
  document.getElementById('eye-slash').style.display = visible ? 'block' : 'none';
});
let noticeTimer, hideTimer;
document.querySelectorAll('[data-login-notice]').forEach(button => button.addEventListener('click', () => {
  clearTimeout(noticeTimer); clearTimeout(hideTimer);
  const toast = document.getElementById('toast');
  toast.classList.remove('notice-fading');
  toast.textContent = button.dataset.loginNotice;
  toast.hidden = false;
  noticeTimer = setTimeout(() => {
    toast.classList.add('notice-fading');
    hideTimer = setTimeout(() => { toast.hidden = true; }, matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : 600);
  }, 4000);
}));
// 회원가입은 없고, 팀이 만든 체험 계정으로만 로그인합니다 (POST /api/auth/login)
const submitButton = document.getElementById('login-submit');
const feedback = document.getElementById('login-feedback');
document.getElementById('login-form').addEventListener('submit', async event => {
  event.preventDefault();
  submitButton.disabled = true;
  feedback.hidden = false;
  feedback.textContent = '로그인하고 있어요…';
  try {
    await request('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: document.getElementById('email').value.trim(), password: password.value })
    });
    location.href = nextPage;
  } catch (error) {
    feedback.textContent = error instanceof TypeError ? '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.' : error.message;
    password.value = '';
    password.focus();
    submitButton.disabled = false;
  }
});
// 이미 로그인했으면 바로 돌아갈 페이지로 보냅니다
getCurrentUser().then(user => {
  if (user) location.replace(nextPage);
  else submitButton.disabled = false;
});
