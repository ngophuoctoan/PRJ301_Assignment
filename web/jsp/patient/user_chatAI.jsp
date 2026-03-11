<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="model.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"PATIENT".equals(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">

<head>
    <%@ include file="/view/layout/dashboard_head.jsp" %>
    <title>Tư vấn AI - Happy Smile</title>
    <style>
        .chat-container {
            height: calc(100vh - 180px);
            background: white;
            border-radius: 15px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
            display: flex;
            flex-direction: column;
            overflow: hidden;
            position: relative;
        }

        .chat-header {
            padding: 20px;
            border-bottom: 1px solid #eef2f6;
            display: flex;
            align-items: center;
            background: linear-gradient(135deg, #ffffff 0%, #fcfdfe 100%);
        }

        .ai-avatar-wrapper {
            width: 48px;
            height: 48px;
            background: linear-gradient(135deg, #4361ee 0%, #3f37c9 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 20px;
            margin-right: 15px;
            box-shadow: 0 4px 10px rgba(67, 97, 238, 0.2);
        }

        .chat-body {
            flex: 1;
            overflow-y: auto;
            padding: 25px;
            background-color: #f8fbfd;
            display: flex;
            flex-direction: column;
            gap: 15px;
        }

        .message-wrapper {
            display: flex;
            max-width: 80%;
            flex-direction: column;
        }

        .message-wrapper.me {
            align-self: flex-end;
            align-items: flex-end;
        }

        .message-wrapper.other {
            align-self: flex-start;
            align-items: flex-start;
        }

        .message-bubble {
            padding: 12px 18px;
            border-radius: 20px;
            font-size: 15px;
            line-height: 1.5;
            white-space: pre-wrap;
            word-break: break-word;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
            position: relative;
        }

        .message-wrapper.me .message-bubble {
            background: linear-gradient(135deg, #4361ee 0%, #3f37c9 100%);
            color: white;
            border-bottom-right-radius: 4px;
        }

        .message-wrapper.other .message-bubble {
            background: white;
            color: #2b3452;
            border-bottom-left-radius: 4px;
            border: 1px solid #eef2f6;
        }

        .typing-indicator {
            align-self: flex-start;
            background: white;
            padding: 10px 15px;
            border-radius: 20px;
            border-bottom-left-radius: 4px;
            border: 1px solid #eef2f6;
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 10px;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
            animation: fadeIn 0.3s ease;
        }

        .typing-dots {
            display: flex;
            gap: 4px;
        }

        .typing-dot {
            width: 6px;
            height: 6px;
            background: #cbd5e1;
            border-radius: 50%;
            animation: typing 1s infinite alternate;
        }

        .typing-dot:nth-child(2) { animation-delay: 0.2s; }
        .typing-dot:nth-child(3) { animation-delay: 0.4s; }

        @keyframes typing {
            from { opacity: 0.3; transform: scale(0.8); }
            to { opacity: 1; transform: scale(1); background: #4361ee; }
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(5px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .chat-footer {
            padding: 20px;
            background: white;
            border-top: 1px solid #eef2f6;
        }

        .chat-input-wrapper {
            display: flex;
            align-items: center;
            background: #f8f9fa;
            border-radius: 30px;
            padding: 8px 15px;
            border: 1px solid #eef2f6;
            transition: border-color 0.3s;
        }

        .chat-input-wrapper:focus-within {
            border-color: #4361ee;
            background: white;
            box-shadow: 0 0 0 4px rgba(67, 97, 238, 0.1);
        }

        .chat-input {
            border: none;
            background: transparent;
            flex: 1;
            padding: 8px;
            font-size: 15px;
            outline: none;
            resize: none;
            max-height: 100px;
            width: 100%;
        }

        .btn-send {
            background: #4361ee;
            color: white;
            border: none;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s;
            flex-shrink: 0;
        }

        .btn-send:hover {
            background: #3f37c9;
            transform: scale(1.05);
        }

        .welcome-state {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100%;
            color: #8e9bb0;
            text-align: center;
            padding: 20px;
        }

        .welcome-icon {
            font-size: 64px;
            color: #e0e7ff;
            margin-bottom: 20px;
        }

        .welcome-title {
            color: #2b3452;
            font-weight: 700;
            margin-bottom: 10px;
        }

        .welcome-subtitle {
            max-width: 400px;
            line-height: 1.6;
        }

        .chat-body::-webkit-scrollbar { width: 6px; }
        .chat-body::-webkit-scrollbar-track { background: transparent; }
        .chat-body::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 10px; }

        @media (max-width: 768px) {
            .chat-container { height: calc(100vh - 120px); }
        }
    </style>
</head>

<body>
    <div class="dashboard-wrapper">
        <%@ include file="/jsp/patient/user_menu.jsp" %>

        <main class="dashboard-main">
            <%@ include file="/jsp/patient/user_header.jsp" %>

            <div class="dashboard-content">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h4 class="mb-0 text-primary fw-bold"><i class="fas fa-robot me-2"></i>Tư vấn AI</h4>
                        <nav aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0 mt-1">
                                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/UserHompageServlet" class="text-decoration-none">Trang chủ</a></li>
                                <li class="breadcrumb-item active" aria-current="page">Tư vấn với AI</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div class="chat-container">
                    <div class="chat-header">
                        <div class="ai-avatar-wrapper">
                            <i class="fas fa-robot"></i>
                        </div>
                        <div>
                            <h5 class="mb-0 fw-bold text-dark">Trợ lý ảo Happy Smile</h5>
                            <small class="text-success"><i class="fas fa-circle me-1" style="font-size: 8px;"></i>Đang trực tuyến</small>
                        </div>
                    </div>

                    <div class="chat-body" id="chatContainer">
                        <div class="welcome-state" id="welcomeMessage">
                            <div class="welcome-icon">
                                <i class="fas fa-stethoscope"></i>
                            </div>
                            <h3 class="welcome-title">Chào mừng bạn đến với Happy Smile</h3>
                            <p class="welcome-subtitle">Tôi là AI tư vấn sức khỏe nha khoa. Bạn đang có vấn đề gì cần hỗ trợ không? Hãy đặt câu hỏi bên dưới nhé!</p>
                        </div>
                    </div>

                    <div class="chat-footer">
                        <div class="chat-input-wrapper">
                            <textarea id="userInput" class="chat-input" placeholder="Nhập câu hỏi của bạn..." rows="1"></textarea>
                            <button type="button" class="btn-send ms-2" id="sendBtn">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <%@ include file="/view/layout/dashboard_scripts.jsp" %>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const chatContainer = document.getElementById('chatContainer');
            const userInput = document.getElementById('userInput');
            const sendBtn = document.getElementById('sendBtn');
            const welcomeMessage = document.getElementById('welcomeMessage');
            let isTyping = false;

            function scrollToBottom() {
                chatContainer.scrollTop = chatContainer.scrollHeight;
            }

            function appendMessage(message, sender) {
                const wrapperClass = sender === 'me' ? 'me' : 'other';
                const wrapper = document.createElement('div');
                wrapper.className = 'message-wrapper ' + wrapperClass;
                
                const bubble = document.createElement('div');
                bubble.className = 'message-bubble';
                bubble.innerHTML = message;
                
                wrapper.appendChild(bubble);
                chatContainer.appendChild(wrapper);
                scrollToBottom();
            }

            function showTypingIndicator() {
                const indicator = document.createElement('div');
                indicator.className = 'typing-indicator';
                indicator.id = 'typingIndicator';
                indicator.innerHTML = `
                    <small class="text-muted">AI đang soạn tin...</small>
                    <div class="typing-dots">
                        <div class="typing-dot"></div>
                        <div class="typing-dot"></div>
                        <div class="typing-dot"></div>
                    </div>
                `;
                chatContainer.appendChild(indicator);
                scrollToBottom();
            }

            function hideTypingIndicator() {
                const indicator = document.getElementById('typingIndicator');
                if (indicator) indicator.remove();
            }

            function handleSendMessage() {
                const message = userInput.value.trim();
                if (!message || isTyping) return;

                if (welcomeMessage) {
                    welcomeMessage.style.display = 'none';
                }

                appendMessage(message, 'me');
                userInput.value = '';
                userInput.style.height = 'auto';
                
                showTypingIndicator();
                isTyping = true;
                sendBtn.disabled = true;

                const formData = new URLSearchParams();
                formData.append('message', message);

                fetch('${pageContext.request.contextPath}/ChatAiServlet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: formData.toString()
                })
                .then(response => response.text())
                .then(data => {
                    hideTypingIndicator();
                    appendMessage(data, 'other');
                    isTyping = false;
                    sendBtn.disabled = false;
                    userInput.focus();
                })
                .catch(error => {
                    console.error('Error:', error);
                    hideTypingIndicator();
                    appendMessage('Xin lỗi, đã có lỗi khi kết nối với máy chủ AI. Vui lòng thử lại sau.', 'other');
                    isTyping = false;
                    sendBtn.disabled = false;
                    userInput.focus();
                });
            }

            sendBtn.addEventListener('click', handleSendMessage);

            userInput.addEventListener('keydown', function (e) {
                if (e.keyCode === 13 && !e.shiftKey) {
                    e.preventDefault();
                    handleSendMessage();
                }
            });

            userInput.addEventListener('input', function () {
                this.style.height = 'auto';
                this.style.height = this.scrollHeight + 'px';
            });

            userInput.focus();
        });
    </script>
</body>

</html>