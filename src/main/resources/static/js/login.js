if (sessionStorage.getItem('token') && sessionStorage.getItem('usuario')) {
  window.location.href = '/front/tarefas.html';
} else {
  sessionStorage.clear();
}

const formLogin = document.getElementById('form-login');

formLogin.addEventListener('submit', evento => {
  evento.preventDefault();

  executar(async () => {
    const campos = formLogin.elements;
    const resposta = await chamarApi('/auth/login', 'POST', {
      email: campos.email.value,
      senha: campos.senha.value,
    });

    sessionStorage.setItem('token', resposta.token);
    sessionStorage.setItem('usuario', JSON.stringify(resposta.usuario));
    window.location.href = '/front/tarefas.html';
  });
});
