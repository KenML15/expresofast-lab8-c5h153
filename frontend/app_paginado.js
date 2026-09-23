let paginaActual = 0;
let totalPaginas = 0;

document.addEventListener('DOMContentLoaded', () => {
    cargarDatos(0);
});

async function cargarDatos(page) {
    const token = sessionStorage.getItem('jwt_token'); // Asumiendo que guardaste el token aquí
    const busqueda = document.getElementById('txtBusqueda').value;
    const size = document.getElementById('cbSize').value;
    const spEstado = document.getElementById('cbSpEstado').value;

    let url = '';
    let usarPaginacion = true;


    if (spEstado !== '') {
        url = `http://localhost:8080/api/v1/envios/procedimiento/${spEstado}`;
        usarPaginacion = false; 
    } else {

        url = `http://localhost:8080/api/v1/envios?page=${page}&size=${size}`;
        if (busqueda) {
            url += `&busqueda=${encodeURIComponent(busqueda)}`;
        }
    }

    try {
        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) throw new Error('Error en la red');
        const data = await response.json();

        if (usarPaginacion) {
            renderizarTabla(data.content);
            actualizarPaginador(data);
        } else {
            renderizarTabla(data);
            ocultarPaginador(data.length);
        }

    } catch (error) {
        console.error('Error:', error);
    }
}

function renderizarTabla(envios) {
    const tbody = document.getElementById('tablaBody');
    tbody.innerHTML = '';

    envios.forEach(envio => {
        tbody.innerHTML += `
            <tr>
                <td>${envio.codigoRastreo}</td>
                <td>${envio.destinatario}</td>
                <td>${envio.direccionDestino}</td>
                <td>${envio.montoFlete}</td>
                <td>${envio.estado}</td>
                <td>${new Date(envio.fechaCreacion).toLocaleDateString()}</td>
            </tr>
        `;
    });
}

function actualizarPaginador(data) {
    document.getElementById('panelPaginacion').style.display = 'flex';
    paginaActual = data.number;
    totalPaginas = data.totalPages;


    const uiPage = data.number + 1;
    document.getElementById('indicadorPagina').textContent = 
        `Página ${uiPage} de ${data.totalPages} (Total: ${data.totalElements} envíos)`;


    document.getElementById('btnPrimera').disabled = data.first;
    document.getElementById('btnAnterior').disabled = data.first;
    document.getElementById('btnSiguiente').disabled = data.last;
    document.getElementById('btnUltima').disabled = data.last;
}

function ocultarPaginador(total) {
    document.getElementById('panelPaginacion').style.display = 'none';
    document.getElementById('indicadorPagina').textContent = `Total devueltos por SP: ${total} envíos`;
    document.getElementById('panelPaginacion').style.display = 'flex';
    
    ['btnPrimera', 'btnAnterior', 'btnSiguiente', 'btnUltima'].forEach(id => {
        document.getElementById(id).style.display = 'none';
    });
}

function cambiarPagina(delta) {
    const nuevaPagina = paginaActual + delta;
    if (nuevaPagina >= 0 && nuevaPagina < totalPaginas) {
        cargarDatos(nuevaPagina);
    }
}

function irUltimaPagina() {
    if (totalPaginas > 0) {
        cargarDatos(totalPaginas - 1);
    }
}

document.getElementById('cbSpEstado').addEventListener('change', () => {
    ['btnPrimera', 'btnAnterior', 'btnSiguiente', 'btnUltima'].forEach(id => {
        document.getElementById(id).style.display = 'inline-block';
    });
    cargarDatos(0);
});