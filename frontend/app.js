const API_BASE = 'http://localhost:8080/api';

let listaEnviosGlobal = [];
let filtroActual = 'TODOS';
let bitacoraGlobal = [];
let envioIdBitacoraActual = null;

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('loginForm')) {
        // Estamos en login.html
        if (obtenerToken()) {
            window.location.href = 'index.html';
        }
        return;
    }

    if (document.getElementById('enviosContainer')) {
        // Estamos en index.html: exigir sesión activa
        if (!obtenerToken()) {
            window.location.href = 'login.html';
            return;
        }
        renderizadoCondicionalPorRol();
        cargarEnvios();
    }
});

/* ============================================================
   AUTENTICACIÓN
   ============================================================ */

function obtenerToken() {
    return localStorage.getItem('jwt_token');
}

function obtenerRoles() {
    try {
        return JSON.parse(localStorage.getItem('roles')) || [];
    } catch {
        return [];
    }
}

function tieneRol(rol) {
    return obtenerRoles().includes(rol);
}

async function iniciarSesion(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorEl = document.getElementById('loginError');
    errorEl.textContent = '';

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Usuario o contraseña incorrectos.');
        }

        localStorage.setItem('jwt_token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('roles', JSON.stringify(data.roles));

        window.location.href = 'index.html';
    } catch (error) {
        errorEl.textContent = error.message;
    }
}

function cerrarSesion() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
    window.location.href = 'login.html';
}

/* Wrapper de fetch que adjunta el header Authorization y maneja 401/403 */
async function fetchWithAuth(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${obtenerToken()}`,
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        cerrarSesion();
        throw new Error('Sesión expirada o sin permisos. Vuelva a iniciar sesión.');
    }

    return response;
}

function renderizadoCondicionalPorRol() {
    const usuarioInfo = document.getElementById('usuarioInfo');
    if (usuarioInfo) {
        usuarioInfo.textContent = `${localStorage.getItem('username')} (${obtenerRoles().join(', ')})`;
    }

    // ROLE_CONDUCTOR no gestiona registro de envíos ni flota
    const formRegistro = document.getElementById('formRegistroEnvio');
    if (formRegistro && tieneRol('ROLE_CONDUCTOR') && !tieneRol('ROLE_ADMIN') && !tieneRol('ROLE_OPERADOR')) {
        formRegistro.style.display = 'none';
    }
}

/* ============================================================
   ENVÍOS
   ============================================================ */

async function cargarEnvios() {
    try {
        const response = await fetchWithAuth(`${API_BASE}/envios/optimizados`);
        if (!response.ok) throw new Error('Error al cargar los envíos optimizados.');
        listaEnviosGlobal = await response.json();
        renderizarEnvios(listaEnviosGlobal);
    } catch (error) {
        console.error('Error:', error);
        alert(error.message || 'No se pudo conectar con el servidor backend.');
    }
}

function renderizarEnvios(envios) {
    const container = document.getElementById('enviosContainer');
    container.innerHTML = '';

    const mostrarBitacora = tieneRol('ROLE_ADMIN') || tieneRol('ROLE_OPERADOR');

    const enviosFiltrados = filtroActual === 'TODOS'
        ? envios
        : envios.filter(e => e.estadoEnvio === filtroActual);

    if (enviosFiltrados.length === 0) {
        container.innerHTML = '<p>No hay envíos registrados para este filtro.</p>';
        return;
    }

    enviosFiltrados.forEach(envio => {
        const card = document.createElement('article');
        card.className = 'envio-card';

        card.innerHTML = `
            <h3>${envio.codigoRastreo}</h3>
            <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
            <p><strong>Peso:</strong> ${envio.pesoKg} kg | <strong>Costo:</strong> ₡${envio.costo}</p>
            <p><strong>Vehículo:</strong> ${envio.placaVehiculo || 'N/A'}</p>
            <p><strong>Conductor:</strong> ${envio.nombreConductor || 'N/A'}</p>
            <div>
                <span class="pill-status status-${envio.estadoEnvio}">${envio.estadoEnvio}</span>
            </div>
            <div class="card-actions">
                <button class="btn-action btn-transito" onclick="cambiarEstado(${envio.id}, 'EN_TRANSITO')">En Tránsito</button>
                <button class="btn-action btn-entregado" onclick="cambiarEstado(${envio.id}, 'ENTREGADO')">Entregado</button>
                ${mostrarBitacora ? `<button class="btn-action btn-bitacora" onclick="verBitacora(${envio.id})">Ver Bitácora</button>` : ''}
            </div>
        `;
        container.appendChild(card);
    });
}

function filtrarEstado(estado, event) {
    filtroActual = estado;
    document.querySelectorAll('.btn-filter').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    renderizarEnvios(listaEnviosGlobal);
}

async function registrarEnvio(event) {
    event.preventDefault();

    const payload = {
        codigoRastreo: document.getElementById('codigoRastreo').value,
        direccionDestino: document.getElementById('direccionDestino').value,
        pesoKg: parseFloat(document.getElementById('pesoKg').value),
        costo: parseFloat(document.getElementById('costo').value),
        vehiculoId: parseInt(document.getElementById('vehiculoId').value),
        conductorId: parseInt(document.getElementById('conductorId').value)
    };

    try {
        const response = await fetchWithAuth(`${API_BASE}/envios`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Error al registrar el envío.');
        }

        alert('Envío registrado con éxito.');
        document.getElementById('envioForm').reset();
        cargarEnvios();
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function cambiarEstado(id, nuevoEstado) {
    const observaciones = prompt('Observaciones para este cambio de estado (opcional):', '') || '';

    try {
        const response = await fetchWithAuth(`${API_BASE}/envios/${id}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado, observaciones })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'No se pudo actualizar el estado.');
        }

        cargarEnvios();
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

/* ============================================================
   BITÁCORA DE AUDITORÍA
   ============================================================ */

async function verBitacora(envioId) {
    envioIdBitacoraActual = envioId;
    document.getElementById('fechaInicio').value = '';
    document.getElementById('fechaFin').value = '';

    try {
        const response = await fetchWithAuth(`${API_BASE}/envios/${envioId}/bitacora`);
        if (!response.ok) throw new Error('No se pudo cargar la bitácora.');

        bitacoraGlobal = await response.json();
        renderizarBitacora(bitacoraGlobal);
        document.getElementById('modalBitacora').classList.remove('oculto');
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function renderizarBitacora(entradas) {
    const container = document.getElementById('bitacoraContainer');

    if (entradas.length === 0) {
        container.innerHTML = '<p>No hay registros de bitácora para este envío.</p>';
        return;
    }

    container.innerHTML = entradas.map(b => `
        <div class="bitacora-item">
            <p><strong>${b.estadoAnterior}</strong> &rarr; <strong>${b.estadoNuevo}</strong></p>
            <p class="bitacora-meta">${new Date(b.fechaCambio).toLocaleString('es-CR')} · ${b.usuario}</p>
            ${b.observaciones ? `<p class="bitacora-obs">${b.observaciones}</p>` : ''}
        </div>
    `).join('');
}

function filtrarBitacoraPorFecha() {
    const desde = document.getElementById('fechaInicio').value;
    const hasta = document.getElementById('fechaFin').value;

    let filtradas = bitacoraGlobal;

    if (desde) {
        filtradas = filtradas.filter(b => b.fechaCambio.slice(0, 10) >= desde);
    }
    if (hasta) {
        filtradas = filtradas.filter(b => b.fechaCambio.slice(0, 10) <= hasta);
    }

    renderizarBitacora(filtradas);
}

function cerrarModalBitacora() {
    document.getElementById('modalBitacora').classList.add('oculto');
    envioIdBitacoraActual = null;
}

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');


    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault(); 

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;


            errorMessage.textContent = '';

            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    
                    sessionStorage.setItem('jwt_token', data.token);
                    
                    window.location.href = 'dashboard.html';
                } else if (response.status === 401 || response.status === 403) {
                    errorMessage.textContent = 'Credenciales incorrectas. Intente nuevamente.';
                } else {
                    errorMessage.textContent = 'Error procesando la solicitud. Código: ' + response.status;
                }
            } catch (error) {
                console.error('Error de red:', error);
                errorMessage.textContent = 'No se pudo conectar con el servidor. ¿Está encendido el backend?';
            }
        });
    }
});

// ==========================================
// Lógica de la Consola de Operaciones
// ==========================================
document.addEventListener('DOMContentLoaded', () => {

    const dashboardHeader = document.querySelector('.dashboard-header');
    
    if (dashboardHeader) {
        const token = sessionStorage.getItem('jwt_token');
        

        if (!token) {
            window.location.href = 'index.html';
            return;
        }


        const payloadBase64 = token.split('.')[1];
        const payloadDecoded = JSON.parse(atob(payloadBase64));
        const username = payloadDecoded.sub;
        

        const roles = payloadDecoded.roles || []; 

        document.getElementById('userName').textContent = username;


        const isAdmin = roles.includes('ROLE_ADMIN');
        const isOperador = roles.includes('ROLE_OPERADOR');
        const isConductor = roles.includes('ROLE_CONDUCTOR');

 
        if (isAdmin) {
            document.getElementById('bitacoraAside').style.display = 'block';
        }


        document.getElementById('btnLogout').addEventListener('click', () => {
            sessionStorage.removeItem('jwt_token');
            window.location.href = 'index.html';
        });

   
        cargarEnvios(token, isOperador, isConductor);
    }
});


async function cargarEnvios(token, isOperador, isConductor) {
    try {
        // Cambiamos /api/envios por /api/envios/optimizados
        const response = await fetch('http://localhost:8080/api/envios/optimizados', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` 
            }
        });


        if (response.status === 401 || response.status === 403) {
            sessionStorage.removeItem('jwt_token');
            window.location.href = 'index.html';
            return;
        }

        if (!response.ok) throw new Error('Fallo al obtener los recursos');

        const envios = await response.json();
        renderizarTarjetas(envios, isOperador, isConductor);
        calcularKPIs(envios);

    } catch (error) {
        console.error('Error de red:', error);
    }
}

function renderizarTarjetas(envios, isOperador, isConductor) {
    const grid = document.getElementById('enviosGrid');
    grid.innerHTML = '';

    envios.forEach(envio => {
        const article = document.createElement('article');
        article.className = `envio-card estado-${envio.estadoEnvio}`;
        
        let btnAccion = '';
        
        if (isOperador && envio.estadoEnvio === 'PENDIENTE') {
            btnAccion = `<button onclick="cambiarEstado(${envio.id}, 'EN_TRANSITO')">Enviar a Tránsito</button>`;
        } else if (isConductor && envio.estadoEnvio === 'EN_TRANSITO') {
            btnAccion = `<button onclick="cambiarEstado(${envio.id}, 'ENTREGADO')">Confirmar Entrega</button>`;
        }

        // Asegúrate de usar las propiedades correctas que viajan desde el backend:
        article.innerHTML = `
            <h3>Rastreo: ${envio.codigoRastreo}</h3>
            <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
            <p><strong>Estado:</strong> ${envio.estadoEnvio}</p>
            <p><strong>Vehículo:</strong> ${envio.placaVehiculo || 'N/A'}</p>
            <p><strong>Conductor:</strong> ${envio.nombreConductor || 'N/A'}</p>
            ${btnAccion}
        `;
        
        grid.appendChild(article);
    });
}


window.cambiarEstado = async function(id, nuevoEstado) {
    const token = sessionStorage.getItem('jwt_token');
    
    try {
        const response = await fetch(`http://localhost:8080/api/envios/${id}/estado`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` 
            },
            body: JSON.stringify({ nuevoEstado: nuevoEstado, observaciones: 'Actualización rápida web' })
        });

        if (response.status === 401 || response.status === 403) {
            sessionStorage.removeItem('jwt_token');
            window.location.href = 'index.html';
            return;
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'No se pudo actualizar el estado.');
        }

        // Si todo sale bien, recargamos la vista
        window.location.reload();

    } catch (error) {
        console.error('Error al cambiar estado:', error);
        alert('Error: ' + error.message);
    }
};

function calcularKPIs(envios) {
    document.getElementById('kpiTotal').textContent = envios.length;
    document.getElementById('kpiEntregados').textContent = envios.filter(e => e.estadoEnvio === 'ENTREGADO').length;
    
   
    const vehiculosEnRuta = new Set(
        envios.filter(e => e.estadoEnvio === 'EN_TRANSITO' && e.placaVehiculo)
              .map(e => e.placaVehiculo)
    );
    document.getElementById('kpiVehiculos').textContent = vehiculosEnRuta.size;
}
