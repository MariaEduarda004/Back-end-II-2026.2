const formCadastro = document.getElementById('form-cadastro');

formCadastro.addEventListener('submit', evento => {
  evento.preventDefault();

  executar(async () => {
    const campos = formCadastro.elements;
    const usuario = {
      nome: campos.nome.value,
      email: campos.email.value,
      idade: Number(campos.idade.value),
      senha: campos.senha.value,
      ativo: true,
    };

    await chamarApi('/usuarios', 'POST', usuario);
    const resposta = await chamarApi('/auth/login', 'POST', {
      email: usuario.email,
      senha: usuario.senha,
    });

    sessionStorage.setItem('token', resposta.token);
    sessionStorage.setItem('usuario', JSON.stringify(resposta.usuario));
    window.location.href = '/front/tarefas.html';
  });
});
