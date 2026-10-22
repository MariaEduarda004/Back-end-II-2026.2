const token = sessionStorage.getItem('token');
const usuario = JSON.parse(sessionStorage.getItem('usuario') || 'null');

if (!token || !usuario) {
  sessionStorage.clear();
  window.location.href = '/';
} else {
  document.getElementById('nome-usuario').textContent = 'Olá, ' + usuario.nome;
  executar(carregarTarefas);
}

const formTarefa = document.getElementById('form-tarefa');
const botaoSalvarTarefa = document.getElementById('botao-salvar-tarefa');
const botaoCancelarEdicao = document.getElementById('botao-cancelar-edicao');
const listaTarefas = document.getElementById('lista-tarefas');
const listaVazia = document.getElementById('lista-vazia');
const contador = document.getElementById('contador');

let tarefas = [];
let filtro = 'todas';
let tarefaEmEdicao = null;

async function carregarTarefas() {
  tarefas = await chamarApi('/tarefas');
  desenharTarefas();
}

function desenharTarefas() {
  let tarefasVisiveis = tarefas;

  if (filtro === 'pendentes') {
    tarefasVisiveis = tarefas.filter(tarefa => !tarefa.concluida);
  } else if (filtro === 'concluidas') {
    tarefasVisiveis = tarefas.filter(tarefa => tarefa.concluida);
  }

  listaTarefas.textContent = '';
  tarefasVisiveis.forEach(tarefa => {
    listaTarefas.appendChild(criarItemDaTarefa(tarefa));
  });
  listaVazia.hidden = tarefasVisiveis.length > 0;

  const pendentes = tarefas.filter(tarefa => !tarefa.concluida).length;
  contador.textContent = pendentes + ' pendente(s) de ' + tarefas.length;
}

function criarItemDaTarefa(tarefa) {
  const item = document.createElement('li');
  item.className = 'tarefa' + (tarefa.concluida ? ' concluida' : '');

  const caixa = document.createElement('input');
  caixa.type = 'checkbox';
  caixa.checked = tarefa.concluida;
  caixa.addEventListener('change', () => alternarConclusao(tarefa));

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

async function alternarConclusao(tarefa) {
  await executar(async () => {
    await chamarApi('/tarefas/' + tarefa.id, 'PUT', {
      titulo: tarefa.titulo,
      concluida: !tarefa.concluida,
    });
    await carregarTarefas();
  });
}

async function excluirTarefa(tarefa) {
  if (!confirm('Excluir a tarefa "' + tarefa.titulo + '"?')) return;

  await executar(async () => {
    await chamarApi('/tarefas/' + tarefa.id, 'DELETE');
    await carregarTarefas();
  }, 'Tarefa excluída.');
}

function iniciarEdicao(tarefa) {
  tarefaEmEdicao = tarefa;
  formTarefa.elements.titulo.value = tarefa.titulo;
  formTarefa.elements.titulo.focus();
  botaoSalvarTarefa.textContent = 'Salvar';
  botaoCancelarEdicao.hidden = false;
}

function finalizarEdicao() {
  tarefaEmEdicao = null;
  formTarefa.reset();
  botaoSalvarTarefa.textContent = 'Adicionar';
  botaoCancelarEdicao.hidden = true;
}

formTarefa.addEventListener('submit', evento => {
  evento.preventDefault();
  const titulo = formTarefa.elements.titulo.value;
  const tarefaEditada = tarefaEmEdicao;

  executar(async () => {
    if (tarefaEditada) {
      await chamarApi('/tarefas/' + tarefaEditada.id, 'PUT', {
        titulo,
        concluida: tarefaEditada.concluida,
      });
    } else {
      await chamarApi('/tarefas', 'POST', { titulo, concluida: false });
    }

    finalizarEdicao();
    await carregarTarefas();
  }, tarefaEditada ? 'Tarefa atualizada.' : 'Tarefa adicionada.');
});

botaoCancelarEdicao.addEventListener('click', finalizarEdicao);

document.querySelectorAll('.filtro').forEach(botao => {
  botao.addEventListener('click', () => {
    filtro = botao.dataset.filtro;
    document.querySelectorAll('.filtro').forEach(outroBotao => {
      outroBotao.classList.toggle('ativa', outroBotao === botao);
    });
    desenharTarefas();
  });
});

document.getElementById('botao-exportar').addEventListener('click', () => {
  executar(async () => {
    const resposta = await fetch('/tarefas/exportar/json', {
      headers: { Authorization: 'Bearer ' + token },
    });
    if (!resposta.ok) {
      if (resposta.status === 401) {
        sessionStorage.clear();
        window.location.href = '/';
      }
      throw new Error('Não foi possível exportar as tarefas.');
    }

    const arquivo = await resposta.blob();
    const link = document.createElement('a');
    link.href = URL.createObjectURL(arquivo);
    link.download = 'tarefas.json';
    link.click();
    URL.revokeObjectURL(link.href);
  }, 'Arquivo tarefas.json gerado.');
});

document.getElementById('botao-sair').addEventListener('click', () => {
  executar(async () => {
    await chamarApi('/auth/logout', 'POST');
    sessionStorage.clear();
    window.location.href = '/';
  });
});
