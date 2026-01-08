let currentUser = null;
let currentCategory = 'all';
let currentPage = 1;

document.addEventListener('DOMContentLoaded', () => {
    checkLoginStatus();
    
    // 初始化老式控制台动画
    initRetroConsole();

    const categoryItems = document.querySelectorAll('.category-item');
    
    // 分类点击事件
    categoryItems.forEach(item => {
        item.addEventListener('click', () => {
            categoryItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            
            currentCategory = item.dataset.id;
            currentPage = 1;
            fetchAndRenderChallenges();
        });
    });
});

async function fetchAndRenderChallenges() {
    let url = `/api/challenges?page=${currentPage}`;
    if (currentCategory !== 'all') {
        url += `&categoryId=${currentCategory}`;
    }
    
    try {
        const response = await fetch(url);
        const data = await response.json();
        renderChallenges(data.challenges);
        renderPagination(data.totalPages, data.currentPage);
    } catch (e) {
        console.error("Failed to load challenges", e);
    }
}

function renderPagination(totalPages, currentPage) {
    let paginationEl = document.querySelector('.pagination');
    if (!paginationEl) {
        paginationEl = document.createElement('div');
        paginationEl.className = 'pagination';
        // Append after #challenge-list
        const challengeList = document.getElementById('challenge-list');
        if (challengeList) {
            challengeList.parentElement.appendChild(paginationEl);
        }
    }
    
    if (totalPages <= 1) {
        paginationEl.style.display = 'none';
        return;
    }
    paginationEl.style.display = 'flex';
    
    let html = '';
    
    if (currentPage > 1) {
        html += `<a href="javascript:void(0)" onclick="changePage(${currentPage - 1})">&laquo; 上一页</a>`;
    }
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPage) {
            html += `<span class="current">${i}</span>`;
        } else {
            html += `<a href="javascript:void(0)" onclick="changePage(${i})">${i}</a>`;
        }
    }
    
    if (currentPage < totalPages) {
        html += `<a href="javascript:void(0)" onclick="changePage(${currentPage + 1})">下一页 &raquo;</a>`;
    }
    
    paginationEl.innerHTML = html;
}

function changePage(page) {
    currentPage = page;
    fetchAndRenderChallenges();
}

// Make changePage global
window.changePage = changePage;

// 老式控制台动画逻辑
function initRetroConsole() {
    const consoleEl = document.getElementById('retro-console');
    if (!consoleEl) return;

    const commands = [
        "INITIALIZING_KERNEL...",
        "LOADING_MODULES...",
        "CHECKING_INTEGRITY...",
        "DECRYPTING_DATA_STREAM...",
        "ESTABLISHING_SECURE_LINK...",
        "SCANNING_PORTS...",
        "UPDATING_FIRMWARE...",
        "MOUNTING_VIRTUAL_DRIVE...",
        "ALLOCATING_MEMORY_PAGES...",
        "BYPASSING_FIREWALL...",
        "PING_REMOTE_HOST...",
        "SYNCHRONIZING_CLOCK...",
        "VERIFYING_USER_TOKEN...",
        "DOWNLOADING_ASSETS..."
    ];

    const spinnerChars = ['|', '/', '-', '\\'];
    let spinnerIndex = 0;
    let currentCmdIndex = 0;
    let charIndex = 0;
    let isDeleting = false;
    let isWaiting = false;
    
    // 多行滚动模式
    const prompt = "C:\\> ";
    let historyLines = [
        "C:\\> SYSTEM_INIT...",
        "INITIALIZING_KERNEL_V4.2...",
        "LOADING_SECURE_MODULES [OK]"
    ];
    
    // 立即渲染初始状态
    render();

    function render() {
        // 只显示最后 8 行
        const displayLines = historyLines.slice(-8);
        consoleEl.innerHTML = displayLines.join('<br>') + (isWaiting ? '' : '<span class="blink">_</span>');
    }

    function update() {
        if (!isWaiting) {
            // 添加新指令
            const cmd = commands[currentCmdIndex];
            historyLines.push(prompt + cmd);
            
            // 模拟命令执行结果
            setTimeout(() => {
                if(Math.random() > 0.7) {
                     historyLines.push("&nbsp;&nbsp;[SUCCESS] PROCESS_COMPLETED");
                } else if(Math.random() > 0.9) {
                     historyLines.push("&nbsp;&nbsp;[WARNING] BYPASS_DETECTED");
                }
                render();
            }, 200);

            currentCmdIndex = (currentCmdIndex + 1) % commands.length;
            isWaiting = true;
            render();
            
            // 随机延迟后执行下一条
            setTimeout(() => {
                isWaiting = false;
                render();
            }, 800 + Math.random() * 1500);
        }
    }
    
    // 添加闪烁光标样式
    const style = document.createElement('style');
    style.innerHTML = `
        .blink { animation: blinker 1s linear infinite; }
        @keyframes blinker { 50% { opacity: 0; } }
    `;
    document.head.appendChild(style);

    setInterval(update, 100);
}

    // Old category click listener removed
    
    // Auth Logic
    // ...

function renderChallenges(challenges) {
    const challengeList = document.getElementById('challenge-list');
    const isVipUser = currentUser && currentUser.isVip;

    challengeList.innerHTML = challenges.map(c => {
        const isLocked = c.isVip && !isVipUser;
        const blurStyle = isLocked ? 'filter: blur(8px); user-select: none;' : '';
        const overlay = isLocked ? `
            <div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; 
                        background: rgba(0,0,0,0.6); display: flex; flex-direction: column;
                        align-items: center; justify-content: center; z-index: 10; color: #fff;">
                <h3 style="color: gold; text-shadow: 0 0 10px gold;">VIP 专属题目</h3>
                <p>仅限会员查看和挑战</p>
                <button onclick="recharge()" style="margin-top: 10px; background: gold; color: #000; border: none; padding: 5px 10px; cursor: pointer;">立即升级</button>
            </div>
        ` : '';

        const solvedBadge = c.isSolved ? `<span class="solved-badge">[ SYSTEM: SOLVED ]</span>` : '';
        const inputDisabled = isLocked || c.isSolved ? 'disabled' : '';
        const buttonText = c.isSolved ? '已完成' : '提交';
        const cardClass = `challenge-card ${c.isSolved ? 'solved' : ''}`;
        const placeholderText = c.isSolved ? '[ SYSTEM: FLAG CAPTURED ]' : 'ctf{...}';

        return `
        <div class="${cardClass}" id="challenge-${c.id}" style="position: relative; overflow: hidden; ${c.isVip ? 'border: 1px solid gold; box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);' : ''}">
            ${overlay}
            <div style="${blurStyle}">
                <div class="challenge-header">
                    <h4 class="challenge-title">
                        ${c.title} 
                        ${c.isVip ? '<span style="color: gold; font-size: 0.8em; border: 1px solid gold; padding: 0 4px; border-radius: 4px;">VIP</span>' : ''}
                        ${solvedBadge}
                    </h4>
                    <div class="challenge-meta">
                        <span class="points-badge">${c.points} 分</span>
                        <span>[${c.categoryName || 'General'}]</span>
                    </div>
                </div>
                <p class="challenge-desc">${c.description}</p>
                ${(() => {
                    if (!c.attachmentUrl) return '';
                    // ... existing logic ...
                    const isImage = /\.(jpg|jpeg|png|gif|webp)$/i.test(c.attachmentUrl);
                    let html = '';
                    if (isImage) {
                        const filename = c.attachmentUrl.split('/').pop();
                        const thumbUrl = `/api/thumbnail/${filename}`;
                        html += `
                            <div class="attachment-box">
                                <img src="${thumbUrl}" alt="Attachment Thumbnail" class="attachment-thumb" loading="lazy" data-url="${c.attachmentUrl}" onclick="window.open(this.dataset.url)" onerror="this.onerror=null;this.src=this.dataset.url">
                                <a href="${c.attachmentUrl}" target="_blank" class="attachment-link">[查看原图]</a>
                            </div>
                        `;
                    }
                    html += `<p><a href="${c.attachmentUrl}" target="_blank" class="attachment-link" download>[下载附件]</a></p>`;
                    return html;
                })()}
                
                ${(() => {
                    // ... existing logic ...
                    const webLinks = {
                        'SQL 注入': '/challenge/web/sqli',
                        'Cookie 伪造': '/challenge/web/cookie',
                        '机器人协议': '/robots.txt'
                    };
                    for (const [key, url] of Object.entries(webLinks)) {
                        if (c.title && c.title.includes(key)) {
                             return `<div style="margin-top:10px; margin-bottom: 10px;">
                                        <a href="${url}" target="_blank" style="display:inline-block; padding:8px 15px; background:#00ff00; color:#000; text-decoration:none; font-weight:bold; border: 1px solid #000; box-shadow: 2px 2px 0 #004400;">
                                            ⚡ 启动靶机 (Launch Instance)
                                        </a>
                                     </div>`;
                        }
                    }
                    return '';
                })()}

                <div class="flag-input-group">
                    <input type="text" placeholder="${placeholderText}" id="input-${c.id}" ${inputDisabled}>
                    <button onclick="submitFlag(${c.id})" ${inputDisabled}>${buttonText}</button>
                </div>
                <div class="message" id="msg-${c.id}"></div>
            </div>
        </div>
    `}).join('');
}

// Auth Logic
async function checkLoginStatus() {
    const res = await fetch('/api/auth/me');
    const data = await res.json();
    const controls = document.getElementById('auth-controls');
    
    if (data.success) {
        currentUser = data.user;
        let html = `<span>欢迎, ${currentUser.username} (${currentUser.score} 分)</span>`;
        html += `<a href="javascript:void(0)" onclick="openModal('profileModal')">个人档案</a>`;
        
        if (currentUser.role === 'ADMIN') {
            html += `<a href="javascript:void(0)" onclick="openModal('adminModal')">管理后台</a>`;
        }
        
        html += `<a href="javascript:void(0)" onclick="logout()">退出</a>`;
        controls.innerHTML = html;
        updateProfileContent();
    } else {
        currentUser = null;
        controls.innerHTML = `
            <a href="javascript:void(0)" onclick="openModal('loginModal')">登录</a>
            <a href="javascript:void(0)" onclick="openModal('registerModal')">注册</a>
        `;
    }
}

async function login() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    const msg = document.getElementById('login-msg');
    
    const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    
    if (data.success) {
        closeModal('loginModal');
        checkLoginStatus();
        window.location.reload(); 
    } else {
        msg.textContent = data.message;
        msg.className = 'message error';
    }
}

async function register() {
    const username = document.getElementById('reg-username').value;
    const password = document.getElementById('reg-password').value;
    const msg = document.getElementById('reg-msg');
    
    const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    const data = await res.json();
    
    if (data.success) {
        msg.textContent = '注册成功！请登录。';
        msg.className = 'message success';
        setTimeout(() => {
            closeModal('registerModal');
            openModal('loginModal');
        }, 1500);
    } else {
        msg.textContent = data.message;
        msg.className = 'message error';
    }
}

async function logout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.reload();
}

function updateProfileContent() {
    if (!currentUser) return;
    const content = document.getElementById('profile-content');
    content.innerHTML = `
        <p><strong>用户名:</strong> ${currentUser.username}</p>
        <p><strong>角色:</strong> ${currentUser.role}</p>
        <p><strong>积分:</strong> ${currentUser.score}</p>
        <p><strong>VIP 状态:</strong> ${currentUser.isVip ? '已激活' : '未激活'}</p>
    `;
}

// Modal Logic
function openModal(id) {
    document.getElementById(id).style.display = "block";
    if (id === 'adminModal') {
        loadUsers();
        loadChallenges();
    }
}

function closeModal(id) {
    document.getElementById(id).style.display = "none";
}

window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = "none";
    }
}

// Admin Logic
async function loadUsers() {
    const res = await fetch('/api/admin/users');
    if (res.ok) {
        const users = await res.json();
        const tbody = document.getElementById('admin-user-list');
        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td>${u.username}</td>
                <td>${u.role}</td>
                <td>${u.score}</td>
                <td>
                    <button onclick="toggleUserVip(${u.id}, ${!u.isVip})" style="background-color: ${u.isVip ? '#28a745' : '#6c757d'}; border: none; padding: 5px 10px; color: white; border-radius: 4px; cursor: pointer;">
                        ${u.isVip ? '已激活' : '未激活'}
                    </button>
                </td>
                <td>
                    <button class="btn-edit" onclick="openEditUserModal(${u.id})">编辑</button>
                    <button class="btn-danger" onclick="deleteUser(${u.id})">删除</button>
                </td>
            </tr>
        `).join('');
    }
}

async function toggleUserVip(id, isVip) {
    const formData = new FormData();
    formData.append('isVip', isVip);
    
    try {
        const res = await fetch(`/api/admin/users/${id}/vip`, {
            method: 'POST',
            body: formData
        });
        const result = await res.json();
        if (result.success) {
            loadUsers(); 
        } else {
            alert('操作失败: ' + result.message);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('操作失败');
    }
}

async function deleteUser(id) {
    if(!confirm('确定要删除该用户吗？')) return;
    await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
    loadUsers();
}

async function openEditUserModal(id) {
    const res = await fetch(`/api/admin/users/${id}`);
    const result = await res.json();
    
    if (result.success) {
        const u = result.data;
        document.getElementById('edit-u-id').value = u.id;
        document.getElementById('edit-u-username').value = u.username;
        document.getElementById('edit-u-password').value = ''; // Don't show password
        document.getElementById('edit-u-role').value = u.role;
        document.getElementById('edit-u-score').value = u.score;
        document.getElementById('edit-u-isvip').checked = u.isVip || false;
        
        openModal('editUserModal');
    } else {
        alert(result.message);
    }
}

async function updateUser() {
    const id = document.getElementById('edit-u-id').value;
    const username = document.getElementById('edit-u-username').value;
    const password = document.getElementById('edit-u-password').value;
    const role = document.getElementById('edit-u-role').value;
    const score = document.getElementById('edit-u-score').value;
    const isVip = document.getElementById('edit-u-isvip').checked;
    
    const formData = new FormData();
    formData.append('username', username);
    formData.append('role', role);
    formData.append('score', score);
    formData.append('isVip', isVip);
    
    if (password) {
        formData.append('password', password);
    }
    
    try {
        const response = await fetch(`/api/admin/users/${id}`, {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert('用户更新成功！');
            closeModal('editUserModal');
            loadUsers();
        } else {
            alert('用户更新失败: ' + result.message);
        }
    } catch (error) {
        console.error('Update error:', error);
        alert('更新失败，请检查网络。');
    }
}

async function loadChallenges() {
    const res = await fetch('/api/challenges?size=1000'); // Load many for admin
    if (res.ok) {
        const data = await res.json();
        const challenges = data.challenges || data; // Handle Map or List
        const tbody = document.getElementById('admin-challenge-list');
        tbody.innerHTML = challenges.map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.title}</td>
                <td>${c.categoryName || c.categoryId}</td>
                <td>${c.points}</td>
                <td>${c.isVip ? '✅' : '❌'}</td>
                <td>
                    <input type="number" value="${c.sortOrder || 0}" style="width: 60px; padding: 2px;" 
                           onchange="updateSortOrder(${c.id}, this.value)">
                </td>
                <td>
                    <button class="btn-edit" onclick="openEditModal(${c.id})">编辑</button>
                    <button class="btn-danger" onclick="deleteChallenge(${c.id})">删除</button>
                </td>
            </tr>
        `).join('');
    }
}

async function openEditModal(id) {
    const res = await fetch(`/api/admin/challenges/${id}`);
    const result = await res.json();
    
    if (result.success) {
        const c = result.data;
        document.getElementById('edit-c-id').value = c.id;
        document.getElementById('edit-c-title').value = c.title;
        document.getElementById('edit-c-desc').value = c.description;
        document.getElementById('edit-c-flag').value = c.flag;
        document.getElementById('edit-c-points').value = c.points;
        document.getElementById('edit-c-cat').value = c.categoryId;
        document.getElementById('edit-c-isvip').checked = c.isVip || false;
        
        const attachmentDiv = document.getElementById('edit-c-current-attachment');
        const deleteCheckbox = document.getElementById('edit-c-delete-attachment');
        const fileInput = document.getElementById('edit-c-file');
        
        fileInput.value = '';
        deleteCheckbox.checked = false;
        
        if (c.attachmentUrl) {
            const fileName = c.attachmentUrl.split('/').pop();
            attachmentDiv.innerHTML = `当前附件: <a href="${c.attachmentUrl}" target="_blank">${fileName}</a>`;
            deleteCheckbox.parentElement.style.display = 'block';
        } else {
            attachmentDiv.innerHTML = '当前无附件';
            deleteCheckbox.parentElement.style.display = 'none';
        }
        
        openModal('editChallengeModal');
    } else {
        alert(result.message);
    }
}

async function updateChallenge() {
    const id = document.getElementById('edit-c-id').value;
    const title = document.getElementById('edit-c-title').value;
    const description = document.getElementById('edit-c-desc').value;
    const flag = document.getElementById('edit-c-flag').value;
    const points = document.getElementById('edit-c-points').value;
    const categoryId = document.getElementById('edit-c-cat').value;
    const isVip = document.getElementById('edit-c-isvip').checked;
    
    const deleteAttachment = document.getElementById('edit-c-delete-attachment').checked;
    const fileInput = document.getElementById('edit-c-file');
    
    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('flag', flag);
    formData.append('points', points);
    formData.append('categoryId', categoryId);
    formData.append('isVip', isVip);
    formData.append('deleteAttachment', deleteAttachment);
    
    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }
    
    try {
        const response = await fetch(`/api/admin/challenges/${id}`, {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert('题目更新成功！');
            closeModal('editChallengeModal');
            loadChallenges();
        } else {
            alert('题目更新失败: ' + result.message);
        }
    } catch (error) {
        console.error('Update error:', error);
        alert('更新失败，请检查网络。');
    }
}

async function updateSortOrder(id, sortOrder) {
    const formData = new FormData();
    formData.append('sortOrder', sortOrder);
    await fetch(`/api/admin/challenges/${id}/sort`, {
        method: 'POST',
        body: formData
    });
}

async function deleteChallenge(id) {
    if(!confirm('确定要删除该题目吗？')) return;
    await fetch(`/api/admin/challenges/${id}`, { method: 'DELETE' });
    loadChallenges();
    // Also refresh the main challenge list if needed, but page reload is simple
    // window.location.reload(); 
}

async function addChallenge() {
    const title = document.getElementById('admin-c-title').value;
    const description = document.getElementById('admin-c-desc').value;
    const flag = document.getElementById('admin-c-flag').value;
    const points = document.getElementById('admin-c-points').value;
    const categoryId = document.getElementById('admin-c-cat').value;
    const isVip = document.getElementById('admin-c-isvip').checked;
    const fileInput = document.getElementById('admin-c-file');
    
    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('flag', flag);
    formData.append('points', points);
    formData.append('categoryId', categoryId);
    formData.append('isVip', isVip);
    
    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }
    
    try {
        const response = await fetch('/api/admin/challenges', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        if (result.success) {
            alert('题目添加成功！');
            window.location.reload();
        } else {
            alert('题目添加失败: ' + result.message);
        }
    } catch (error) {
        console.error('Upload error:', error);
        alert('上传失败，请检查网络或文件大小是否超出服务器限制。');
    }
}

// Existing Logic
async function submitFlag(id) {
    if (!currentUser) {
        alert('请先登录！');
        openModal('loginModal');
        return;
    }

    const flagInput = document.getElementById(`input-${id}`);
    const msgDiv = document.getElementById(`msg-${id}`);
    const flag = flagInput.value.trim();

    if (!flag) return;

    try {
        const response = await fetch('/api/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                id: id, 
                flag: flag,
                userId: currentUser.id 
            })
        });
        const result = await response.json();

        msgDiv.textContent = result.message;
        msgDiv.className = 'message ' + (result.success ? 'success' : 'error');
        
        if (result.success) {
            flagInput.disabled = true;
            flagInput.value = ''; // Clear input
            flagInput.placeholder = '[ SYSTEM: FLAG CAPTURED ]';
            
            const btn = flagInput.nextElementSibling;
            if(btn && btn.tagName === 'BUTTON') {
                btn.disabled = true;
                btn.textContent = '已完成';
            }
            
            // 添加已完成样式类到卡片
            const card = document.getElementById(`challenge-${id}`);
            if (card) {
                card.classList.add('solved');
                // 可选：如果需要在标题旁添加 badge，也可以在这里动态添加，
                // 但通常刷新页面或重新渲染列表更简单。
                // 为了即时效果，我们可以尝试查找标题并添加
                const title = card.querySelector('.challenge-title');
                if (title && !title.querySelector('.solved-badge')) {
                     const badge = document.createElement('span');
                     badge.className = 'solved-badge';
                     badge.textContent = '[ SYSTEM: SOLVED ]';
                     title.appendChild(badge);
                }
            }

            checkLoginStatus(); // Update score in UI
            refreshRankings();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function recharge() {
    if (!currentUser) {
        alert('请先登录');
        openModal('loginModal');
        return;
    }
    // 重置模态框状态
    document.getElementById('payment-step-1').style.display = 'block';
    document.getElementById('payment-result').style.display = 'none';
    
    openModal('paymentModal');
    
    // 初始化 PayPal 按钮（如果尚未初始化）
    if (document.getElementById('paypal-button-container').innerHTML === "") {
        document.getElementById('paypal-button-container').innerHTML = '<p style="text-align:center;">正在加载支付组件...</p>';
        loadPayPalScript()
            .then(() => {
                // 清除加载提示
                document.getElementById('paypal-button-container').innerHTML = "";
                initPayPalButton();
            })
            .catch(err => {
                console.error(err);
                document.getElementById('paypal-button-container').innerHTML = '<p style="color:red; text-align:center;">加载失败，请检查网络</p>';
            });
    }
}

function loadPayPalScript() {
    return new Promise((resolve, reject) => {
        if (window.paypal) {
            resolve();
            return;
        }
        const script = document.createElement('script');
        script.src = "https://www.paypal.com/sdk/js?client-id=sb&currency=USD";
        script.async = true;
        script.onload = resolve;
        script.onerror = () => reject(new Error('PayPal SDK failed to load'));
        document.head.appendChild(script);
    });
}

function initPayPalButton() {
    paypal.Buttons({
        // 创建订单
        createOrder: function(data, actions) {
            return fetch('/api/payment/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: currentUser.id })
            }).then(function(res) {
                return res.json();
            }).then(function(orderData) {
                if (orderData.success) {
                    return orderData.orderId; // 返回 PayPal Order ID
                } else {
                    alert('Create Order Error: ' + orderData.message);
                    throw new Error(orderData.message);
                }
            });
        },

        // 批准订单（支付成功回调）
        onApprove: function(data, actions) {
            return fetch('/api/payment/capture', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ orderId: data.orderID })
            }).then(function(res) {
                return res.json();
            }).then(function(orderData) {
                if (orderData.success) {
                    // 显示成功消息
                    document.getElementById('payment-step-1').style.display = 'none';
                    document.getElementById('payment-result').style.display = 'block';
                    document.getElementById('payment-message').textContent = '支付成功！您已成为 VIP 用户。';
                    document.getElementById('payment-message').className = 'message success';
                    
                    checkLoginStatus();
                } else {
                    alert('Capture Error: ' + orderData.message);
                }
            });
        },

        // 错误处理
        onError: function(err) {
            console.error(err);
            alert('PayPal Error: ' + err);
        }
    }).render('#paypal-button-container');
}

// 已废弃的旧函数，保留以防万一或作为参考
async function createOrder() { /* ... */ }
async function confirmPayment() { /* ... */ }

async function refreshRankings() {
    const response = await fetch('/api/rankings');
    const rankings = await response.json();
    const rankBody = document.getElementById('rank-body');
    rankBody.innerHTML = rankings.map((user, index) => `
        <tr>
            <td>${index + 1}</td>
            <td>
                ${user.username}
                ${user.isVip ? '<span class="vip-tag">VIP</span>' : ''}
            </td>
            <td>${user.score}</td>
        </tr>
    `).join('');
}
