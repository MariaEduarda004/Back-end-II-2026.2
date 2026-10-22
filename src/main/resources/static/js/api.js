async function chamarApi(url, metodo = 'GET', corpo) {
  const opcoes = { method: metodo, headers: {} };
  const token = sessionStorage.getItem('token');

  if (token) {
    opcoes.headers.Authorization = 'Bearer ' + token;
  }
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json';
    opcoes.body = JSON.stringify(corpo);
  }

  const resposta = await fetch(url, opcoes);
  if (resposta.status === 204) return null;

  const dados = await resposta.json().catch(() => null);
  if (!resposta.ok) {
    if (resposta.status === 401 && token) {
      sessionStorage.clear();
      window.location.href = '/';
    }

    let mensagem = dados && dados.erro ? dados.erro : 'Erro ' + resposta.status;
    if (dados && dados.campos) {
      const detalhes = Object.values(dados.campos).join('; ');
      if (detalhes) mensagem += ': ' + detalhes;
    }
    throw new Error(mensagem);
  }

  return dados;
}

function mostrarMensagem(texto, tipo = 'erro') {
  const caixa = document.getElementById('mensagem');
  caixa.textContent = texto;
  caixa.className = tipo;
  caixa.hidden = false;
}

async function executar(acao, mensagemDeSucesso) {
  try {
    await acao();
    if (mensagemDeSucesso) mostrarMensagem(mensagemDeSucesso, 'ok');
  } catch (erro) {
    mostrarMensagem(erro.message, 'erro');
  }
}
