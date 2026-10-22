const telaLogin = document.getElementById('tela-login');
const telaTarefas = document.getElementById('tela-tarefas');
const caixaMensagem = document.getElementById('mensagem');

const formLogin = document.getElementById('form-login');
const formCadastro = document.getElementById('form-cadastro');
const abaEntrar = document.getElementById('aba-entrar');
const abaCadastrar = document.getElementById('aba-cadastrar');

const formTarefa = document.getElementById('form-tarefa');
const botaoSalvarTarefa = document.getElementById('botao-salvar-tarefa');
const botaoCancelarEdicao = document.getElementById('botao-cancelar-edicao');
const listaTarefas = document.getElementById('lista-tarefas');
const listaVazia = document.getElementById('lista-vazia');
const contador = document.getElementById('contador');

// O token fica no sessionStorage: some quando a aba é fechada
const estado = {
  token: sessionStorage.getItem('token'),
  usuario: JSON.parse(sessionStorage.getItem('usuario') || 'null'),
  tarefas: [],
  filtro: 'todas',
  tarefaEmEdicao: null,
};

/* ================= 2. Comunicação com a API ================= */

// Envia a requisição, já com o token e o JSON. Devolve o corpo da resposta ou lança um Error
// com a mensagem enviada pelo back-end.
async function chamarApi(url, metodo = 'GET', corpo) {
  const opcoes = { method: metodo, headers: {} };

  if (estado.token) {
    opcoes.headers['Authorization'] = 'Bearer ' + estado.token;
  }
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json';
    opcoes.body = JSON.stringify(corpo);
  }

  const resposta = await fetch(url, opcoes);
  if (resposta.status === 204) return null;

  const dados = await resposta.json().catch(() => null);

  if (!resposta.ok) {
    // Sessão vencida: volta para a tela de login
    if (resposta.status === 401 && estado.token) {
      encerrarSessao();
    }
    throw new Error(montarMensagemDeErro(dados, resposta.status));
  }
  return dados;
}

function montarMensagemDeErro(dados, status) {
  if (!dados) return 'Erro ' + status;

  let detalhes = '';
  if (dados.campos) {
    for (const campo in dados.campos) {
      if (detalhes) detalhes += '; ';
      detalhes += dados.campos[campo];
    }
  }

  if (detalhes) return dados.erro + ': ' + detalhes;
  return dados.erro;
}

/* ================= 3. Mensagens e troca de telas ================= */

let temporizadorMensagem;

function mostrarMensagem(texto, tipo) {
  caixaMensagem.textContent = texto;
  caixaMensagem.className = tipo;
  caixaMensagem.hidden = false;

  clearTimeout(temporizadorMensagem);
  temporizadorMensagem = setTimeout(() => { caixaMensagem.hidden = true; }, 4000);
}

// Executa uma ação e mostra na tela qualquer erro que aconteça
async function executar(acao, mensagemDeSucesso) {
  try {
    await acao();
    if (mensagemDeSucesso) mostrarMensagem(mensagemDeSucesso, 'ok');
  } catch (erro) {
    mostrarMensagem(erro.message, 'erro');
  }
}

function mostrarTelaLogin() {
  telaLogin.hidden = false;
  telaTarefas.hidden = true;
}

function mostrarTelaTarefas() {
  telaLogin.hidden = true;
  telaTarefas.hidden = false;
  document.getElementById('nome-usuario').textContent = 'Olá, ' + estado.usuario.nome;
}

/* ================= 4. Login e cadastro ================= */

function escolherAba(aba) {
  const entrando = aba === 'entrar';
  formLogin.hidden = !entrando;
  formCadastro.hidden = entrando;
  abaEntrar.classList.toggle('ativa', entrando);
  abaCadastrar.classList.toggle('ativa', !entrando);
}

async function entrar(email, senha) {
  const resposta = await chamarApi('/auth/login', 'POST', { email, senha });

  estado.token = resposta.token;
  estado.usuario = resposta.usuario;
  sessionStorage.setItem('token', estado.token);
  sessionStorage.setItem('usuario', JSON.stringify(estado.usuario));

  await abrirTelaDeTarefas();
}

async function cadastrar(dados) {
  const novoUsuario = {
    nome: dados.nome,
    email: dados.email,
    idade: dados.idade,
    senha: dados.senha,
    ativo: true,
  };

  await chamarApi('/usuarios', 'POST', novoUsuario);
  // Depois de criar a conta, já entra com ela
  await entrar(dados.email, dados.senha);
}

function encerrarSessao() {
  estado.token = null;
  estado.usuario = null;
  estado.tarefas = [];
  sessionStorage.clear();
  mostrarTelaLogin();
}

abaEntrar.addEventListener('click', () => escolherAba('entrar'));
abaCadastrar.addEventListener('click', () => escolherAba('cadastrar'));

formLogin.addEventListener('submit', evento => {
  evento.preventDefault();
  const campos = formLogin.elements;
  executar(async () => {
    await entrar(campos.email.value, campos.senha.value);
    formLogin.reset();
  });
});

formCadastro.addEventListener('submit', evento => {
  evento.preventDefault();
  const campos = formCadastro.elements;
  executar(async () => {
    await cadastrar({
      nome: campos.nome.value,
      email: campos.email.value,
      idade: Number(campos.idade.value),
      senha: campos.senha.value,
    });
    formCadastro.reset();
  }, 'Conta criada com sucesso!');
});

document.getElementById('botao-sair').addEventListener('click', () => {
  executar(async () => {
    await chamarApi('/auth/logout', 'POST');
    encerrarSessao();
  });
});

/* ================= 5. Tarefas ================= */

// Pede o JSON ao servidor e faz o navegador baixar como arquivo
// (o token vai no cabeçalho, por isso não dá para usar um simples link)
async function exportarTarefasEmJson() {
  const resposta = await fetch('/tarefas/exportar/json', {
    headers: { 'Authorization': 'Bearer ' + estado.token },
  });
  if (!resposta.ok) throw new Error('Não foi possível exportar as tarefas.');

  const arquivo = await resposta.blob();
  const link = document.createElement('a');
  link.href = URL.createObjectURL(arquivo);
  link.download = 'tarefas.json';
  link.click();
  URL.revokeObjectURL(link.href);
}

document.getElementById('botao-exportar').addEventListener('click', () => {
  executar(exportarTarefasEmJson, 'Arquivo tarefas.json gerado.');
});

async function abrirTelaDeTarefas() {
  mostrarTelaTarefas();
  await carregarTarefas();
}

async function carregarTarefas() {
  estado.tarefas = await chamarApi('/tarefas');
  desenharTarefas();
}

// Aplica o filtro escolhido (todas / pendentes / concluídas)
function tarefasVisiveis() {
  if (estado.filtro === 'todas') return estado.tarefas;

  const tarefas = [];
  for (let i = 0; i < estado.tarefas.length; i++) {
    const tarefa = estado.tarefas[i];

    if (estado.filtro === 'pendentes' && !tarefa.concluida) {
      tarefas.push(tarefa);
    }
    if (estado.filtro === 'concluidas' && tarefa.concluida) {
      tarefas.push(tarefa);
    }
  }
  return tarefas;
}

function desenharTarefas() {
  const visiveis = tarefasVisiveis();

  listaTarefas.textContent = '';
  for (let i = 0; i < visiveis.length; i++) {
    const item = criarItemDaTarefa(visiveis[i]);
    listaTarefas.appendChild(item);
  }

  listaVazia.hidden = visiveis.length > 0;

  let pendentes = 0;
  for (let i = 0; i < estado.tarefas.length; i++) {
    if (!estado.tarefas[i].concluida) pendentes++;
  }
  contador.textContent = pendentes + ' pendente(s) de ' + estado.tarefas.length;
}

function criarItemDaTarefa(tarefa) {
  const item = document.createElement('li');
  item.className = 'tarefa' + (tarefa.concluida ? ' concluida' : '');

  const caixa = document.createElement('input');
  caixa.type = 'checkbox';
  caixa.checked = tarefa.concluida;
  caixa.addEventListener('change', () => alternarConclusao(tarefa));

  // textContent (e não innerHTML) evita que um título malicioso execute código na página
  const titulo = document.createElement('span');
  titulo.className = 'titulo';
  titulo.textContent = tarefa.titulo;

  const botaoEditar = criarBotao('Editar', 'secundario', () => iniciarEdicao(tarefa));
  const botaoExcluir = criarBotao('Excluir', 'perigo', () => excluirTarefa(tarefa));

  item.append(caixa, titulo, botaoEditar, botaoExcluir);
  return item;
}

function criarBotao(texto, classe, aoClicar) {
  const botao = document.createElement('button');
  botao.type = 'button';
  botao.textContent = texto;
  botao.className = classe;
  botao.addEventListener('click', aoClicar);
  return botao;
}

function alternarConclusao(tarefa) {
  executar(async () => {
    await chamarApi('/tarefas/' + tarefa.id, 'PUT',
      { titulo: tarefa.titulo, concluida: !tarefa.concluida });
    await carregarTarefas();
  });
}

function excluirTarefa(tarefa) {
  if (!confirm('Excluir a tarefa "' + tarefa.titulo + '"?')) return;

  executar(async () => {
    await chamarApi('/tarefas/' + tarefa.id, 'DELETE');
    await carregarTarefas();
  }, 'Tarefa excluída.');
}

/* ----- Edição de título: reaproveita o formulário de cima ----- */

function iniciarEdicao(tarefa) {
  estado.tarefaEmEdicao = tarefa;
  formTarefa.elements.titulo.value = tarefa.titulo;
  formTarefa.elements.titulo.focus();
  botaoSalvarTarefa.textContent = 'Salvar';
  botaoCancelarEdicao.hidden = false;
}

function finalizarEdicao() {
  estado.tarefaEmEdicao = null;
  formTarefa.reset();
  botaoSalvarTarefa.textContent = 'Adicionar';
  botaoCancelarEdicao.hidden = true;
}

formTarefa.addEventListener('submit', evento => {
  evento.preventDefault();
  const titulo = formTarefa.elements.titulo.value;
  const emEdicao = estado.tarefaEmEdicao;

  executar(async () => {
    if (emEdicao) {
      await chamarApi('/tarefas/' + emEdicao.id, 'PUT', { titulo, concluida: emEdicao.concluida });
    } else {
      await chamarApi('/tarefas', 'POST', { titulo, concluida: false });
    }
    finalizarEdicao();
    await carregarTarefas();
  }, emEdicao ? 'Tarefa atualizada.' : 'Tarefa adicionada.');
});

botaoCancelarEdicao.addEventListener('click', finalizarEdicao);

/* ----- Filtros ----- */

const botoesDeFiltro = document.querySelectorAll('.filtro');
for (let i = 0; i < botoesDeFiltro.length; i++) {
  const botao = botoesDeFiltro[i];
  botao.addEventListener('click', () => {
    estado.filtro = botao.dataset.filtro;
    for (let j = 0; j < botoesDeFiltro.length; j++) {
      const outroBotao = botoesDeFiltro[j];
      outroBotao.classList.toggle('ativa', outroBotao === botao);
    }
    desenharTarefas();
  });
}

/* ================= 6. Inicialização ================= */

// Se já havia login nesta aba, vai direto para as tarefas
if (estado.token && estado.usuario) {
  executar(abrirTelaDeTarefas);
} else {
  mostrarTelaLogin();
}
