/**
 * RAG Corporativo - Frontend Application
 * Interage com o backend Spring Boot (Chat & Ingestão de Documentos)
 */

document.addEventListener('DOMContentLoaded', () => {
  // --- Endpoints da API (Rotas relativas que usam automaticamente o domínio do Railway) ---
  const ENDPOINTS = {
    chat: '/api/v1/chat',
    upload: '/api/v1/documents/upload'
  };

  // --- Elementos do DOM ---
  // Navegação e Views
  const tabChatBtn = document.getElementById('tabChatBtn');
  const tabUploadBtn = document.getElementById('tabUploadBtn');
  const openUploadModalBtn = document.getElementById('openUploadModalBtn');
  const chatView = document.getElementById('chatView');
  const uploadView = document.getElementById('uploadView');
  const mobileMenuBtn = document.getElementById('mobileMenuBtn');
  const mobileMenuBtn2 = document.getElementById('mobileMenuBtn2');
  const sidebar = document.querySelector('.sidebar');

  // Chat
  const chatMessages = document.getElementById('chatMessages');
  const chatForm = document.getElementById('chatForm');
  const questionInput = document.getElementById('questionInput');
  const sendBtn = document.getElementById('sendBtn');
  const clearChatBtn = document.getElementById('clearChatBtn');
  const chatCategoryFilter = document.getElementById('chatCategoryFilter');
  const activeCategoryTag = document.getElementById('activeCategoryTag');
  const activeCategoryName = document.getElementById('activeCategoryName');
  const removeCategoryTag = document.getElementById('removeCategoryTag');
  const promptChips = document.querySelectorAll('.prompt-chip');

  // Upload
  const uploadForm = document.getElementById('uploadForm');
  const dropZone = document.getElementById('dropZone');
  const fileInput = document.getElementById('fileInput');
  const filePreview = document.getElementById('filePreview');
  const fileName = document.getElementById('fileName');
  const removeFileBtn = document.getElementById('removeFileBtn');
  const uploadSubmitBtn = document.getElementById('uploadSubmitBtn');
  const uploadProgress = document.getElementById('uploadProgress');
  const uploadAlert = document.getElementById('uploadAlert');
  const serverUrlDisplay = document.getElementById('serverUrlDisplay');

  if (serverUrlDisplay) {
    serverUrlDisplay.textContent = window.location.origin;
  }

  // --- Funções de Navegação de Abas ---
  function switchView(viewName) {
    if (viewName === 'chat') {
      chatView.classList.add('active');
      uploadView.classList.remove('active');
      tabChatBtn.classList.add('active');
      tabUploadBtn.classList.remove('active');
      questionInput.focus();
    } else if (viewName === 'upload') {
      uploadView.classList.add('active');
      chatView.classList.remove('active');
      tabUploadBtn.classList.add('active');
      tabChatBtn.classList.remove('active');
    }
    // Fechar menu mobile se estiver aberto
    sidebar.classList.remove('open');
  }

  tabChatBtn.addEventListener('click', () => switchView('chat'));
  tabUploadBtn.addEventListener('click', () => switchView('upload'));
  if (openUploadModalBtn) {
    openUploadModalBtn.addEventListener('click', () => switchView('upload'));
  }

  // Mobile Menu Toggle
  function toggleMobileMenu() {
    sidebar.classList.toggle('open');
  }
  if (mobileMenuBtn) mobileMenuBtn.addEventListener('click', toggleMobileMenu);
  if (mobileMenuBtn2) mobileMenuBtn2.addEventListener('click', toggleMobileMenu);

  // --- Filtro de Categoria do Chat ---
  chatCategoryFilter.addEventListener('change', (e) => {
    const value = e.target.value;
    if (value) {
      activeCategoryName.textContent = chatCategoryFilter.options[chatCategoryFilter.selectedIndex].text;
      activeCategoryTag.style.display = 'flex';
    } else {
      activeCategoryTag.style.display = 'none';
    }
  });

  removeCategoryTag.addEventListener('click', () => {
    chatCategoryFilter.value = '';
    activeCategoryTag.style.display = 'none';
  });

  // --- Sugestões Rápidas de Prompt ---
  promptChips.forEach(chip => {
    chip.addEventListener('click', () => {
      const prompt = chip.getAttribute('data-prompt');
      questionInput.value = prompt;
      switchView('chat');
      adjustTextareaHeight();
      questionInput.focus();
    });
  });

  // --- Auto-resize do Textarea ---
  function adjustTextareaHeight() {
    questionInput.style.height = 'auto';
    questionInput.style.height = `${Math.min(questionInput.scrollHeight, 140)}px`;
  }
  questionInput.addEventListener('input', adjustTextareaHeight);

  // Enviar com Enter (sem Shift)
  questionInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      chatForm.dispatchEvent(new Event('submit', { cancelable: true }));
    }
  });

  // --- Chat Logic ---
  chatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const question = questionInput.value.trim();
    if (!question) return;

    const category = chatCategoryFilter.value || null;

    // Adiciona mensagem do usuário
    appendUserMessage(question);
    questionInput.value = '';
    adjustTextareaHeight();

    // Desativa botão e adiciona loading
    sendBtn.disabled = true;
    const loadingMessageElem = appendTypingIndicator();

    try {
      const response = await fetch(ENDPOINTS.chat, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          question: question,
          category: category
        })
      });

      if (!response.ok) {
        throw new Error(`Erro na requisição: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      // Remove loading indicator
      loadingMessageElem.remove();

      // Renderiza resposta do bot
      appendBotMessage(data.answer, data.sources);

    } catch (error) {
      console.error('Falha ao obter resposta do assistente:', error);
      loadingMessageElem.remove();
      appendErrorMessage(`Desculpe, ocorreu um erro ao se comunicar com o backend: ${error.message}. Verifique se o servidor Spring Boot e o Ollama estão em execução.`);
    } finally {
      sendBtn.disabled = false;
      questionInput.focus();
    }
  });

  function appendUserMessage(text) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message user-message';
    msgDiv.innerHTML = `
      <div class="avatar user-avatar">
        <i class="fa-solid fa-user"></i>
      </div>
      <div class="message-content">
        <p>${escapeHtml(text).replace(/\n/g, '<br>')}</p>
      </div>
    `;
    chatMessages.appendChild(msgDiv);
    scrollToBottom();
  }

  function appendBotMessage(answer, sources) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message bot-message';

    // Formata Markdown se marked.js estiver presente, senão usa texto puro seguro
    let formattedAnswer = '';
    if (typeof marked !== 'undefined' && marked.parse) {
      formattedAnswer = marked.parse(answer);
    } else {
      formattedAnswer = `<p>${escapeHtml(answer).replace(/\n/g, '<br>')}</p>`;
    }

    // Monta a lista de fontes citadas
    let sourcesHtml = '';
    if (sources && sources.length > 0) {
      const sourceBadges = sources.map(src => `<span class="source-tag"><i class="fa-solid fa-file-lines"></i> ${escapeHtml(src)}</span>`).join('');
      sourcesHtml = `
        <div class="sources-container">
          <div class="sources-label"><i class="fa-solid fa-bookmark"></i> Fontes Consultadas:</div>
          <div class="sources-list">${sourceBadges}</div>
        </div>
      `;
    }

    msgDiv.innerHTML = `
      <div class="avatar bot-avatar">
        <i class="fa-solid fa-robot"></i>
      </div>
      <div class="message-content">
        ${formattedAnswer}
        ${sourcesHtml}
      </div>
    `;
    chatMessages.appendChild(msgDiv);
    scrollToBottom();
  }

  function appendErrorMessage(errorText) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message bot-message';
    msgDiv.innerHTML = `
      <div class="avatar bot-avatar" style="background: var(--danger);">
        <i class="fa-solid fa-triangle-exclamation"></i>
      </div>
      <div class="message-content" style="border-color: rgba(239, 68, 68, 0.4);">
        <p style="color: #fca5a5;"><strong>Erro:</strong> ${escapeHtml(errorText)}</p>
      </div>
    `;
    chatMessages.appendChild(msgDiv);
    scrollToBottom();
  }

  function appendTypingIndicator() {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message bot-message';
    msgDiv.innerHTML = `
      <div class="avatar bot-avatar">
        <i class="fa-solid fa-robot"></i>
      </div>
      <div class="message-content">
        <div class="typing-indicator">
          <div class="typing-dot"></div>
          <div class="typing-dot"></div>
          <div class="typing-dot"></div>
        </div>
      </div>
    `;
    chatMessages.appendChild(msgDiv);
    scrollToBottom();
    return msgDiv;
  }

  function scrollToBottom() {
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  // Limpar Chat
  clearChatBtn.addEventListener('click', () => {
    if (confirm('Deseja limpar todo o histórico da conversa atual?')) {
      chatMessages.innerHTML = `
        <div class="message system-welcome">
          <div class="avatar bot-avatar">
            <i class="fa-solid fa-robot"></i>
          </div>
          <div class="message-content">
            <h3>Histórico limpo!</h3>
            <p>Faça uma nova pergunta sobre os documentos corporativos.</p>
          </div>
        </div>
      `;
    }
  });

  // --- Document Upload Logic ---
  // Drag & Drop
  ['dragenter', 'dragover'].forEach(eventName => {
    dropZone.addEventListener(eventName, (e) => {
      e.preventDefault();
      e.stopPropagation();
      dropZone.classList.add('dragover');
    }, false);
  });

  ['dragleave', 'drop'].forEach(eventName => {
    dropZone.addEventListener(eventName, (e) => {
      e.preventDefault();
      e.stopPropagation();
      dropZone.classList.remove('dragover');
    }, false);
  });

  dropZone.addEventListener('drop', (e) => {
    const dt = e.dataTransfer;
    const files = dt.files;
    if (files.length > 0) {
      handleFileSelected(files[0]);
    }
  });

  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      handleFileSelected(e.target.files[0]);
    }
  });

  function handleFileSelected(file) {
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      showToast('Por favor, selecione apenas arquivos PDF.', 'error');
      fileInput.value = '';
      return;
    }
    fileName.textContent = `${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)`;
    filePreview.style.display = 'inline-flex';
  }

  removeFileBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    fileInput.value = '';
    filePreview.style.display = 'none';
  });

  // Submit Upload Form
  uploadForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!fileInput.files || fileInput.files.length === 0) {
      showToast('Selecione um arquivo PDF para upload.', 'error');
      return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('category', document.getElementById('docCategory').value);
    formData.append('author', document.getElementById('docAuthor').value);

    // Atualiza UI para loading
    uploadSubmitBtn.disabled = true;
    uploadProgress.style.display = 'flex';
    uploadAlert.style.display = 'none';

    try {
      const response = await fetch(ENDPOINTS.upload, {
        method: 'POST',
        body: formData
      });

      const result = await response.json();

      if (response.ok) {
        uploadAlert.className = 'alert alert-success';
        uploadAlert.innerHTML = `<i class="fa-solid fa-circle-check"></i> ${result.message || 'Documento indexado com sucesso!'}`;
        uploadAlert.style.display = 'flex';
        showToast('Documento indexado com sucesso no PGVector!', 'success');
        
        // Limpar formulário
        uploadForm.reset();
        filePreview.style.display = 'none';
      } else {
        uploadAlert.className = 'alert alert-error';
        uploadAlert.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> ${result.error || 'Falha ao processar o arquivo.'}`;
        uploadAlert.style.display = 'flex';
        showToast(result.error || 'Falha no processamento do documento.', 'error');
      }
    } catch (error) {
      console.error('Erro no upload:', error);
      uploadAlert.className = 'alert alert-error';
      uploadAlert.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> Erro ao conectar ao servidor: ${error.message}`;
      uploadAlert.style.display = 'flex';
      showToast(`Erro na requisição: ${error.message}`, 'error');
    } finally {
      uploadSubmitBtn.disabled = false;
      uploadProgress.style.display = 'none';
    }
  });

  // --- Utilitários ---
  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const icon = type === 'success' 
      ? '<i class="fa-solid fa-circle-check" style="color: var(--success);"></i>'
      : '<i class="fa-solid fa-circle-exclamation" style="color: var(--danger);"></i>';

    toast.innerHTML = `${icon} <span>${escapeHtml(message)}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(50px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }
});
