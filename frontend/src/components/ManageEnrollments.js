import React, { useState, useEffect } from 'react'; // <-- Importar useEffect
import api from '../services/api';

const ManageEnrollments = () => {
  // O estado agora é para a LISTA de matrículas
  const [myEnrollments, setMyEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  /**
   * Helper para extrair a mensagem de erro da resposta da API
   */
  const getErrorMessage = (err, defaultMessage) => {
    if (err.response && err.response.data) {
      if (typeof err.response.data === 'object' && err.response.data.message) {
        return err.response.data.message;
      }
      if (typeof err.response.data === 'string') {
        return err.response.data;
      }
    }
    return defaultMessage;
  };

  // --- NOVA FUNÇÃO ---
  // Busca as matrículas do aluno logado
  const fetchMyEnrollments = async () => {
    setLoading(true);
    setMessage('');
    setError('');
    try {
      const response = await api.get('/enrollments/my-courses');
      setMyEnrollments(response.data);
      if (response.data.length === 0) {
        setMessage('Você não está matriculado em nenhum curso este semestre.');
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Erro ao buscar suas matrículas.'));
    } finally {
      setLoading(false);
    }
  };

  // --- RODA A FUNÇÃO QUANDO O COMPONENTE CARREGA ---
  useEffect(() => {
    fetchMyEnrollments();
  }, []); // O array vazio [] faz rodar só uma vez

  // --- FUNÇÃO DE CANCELAR ATUALIZADA ---
  const handleCancel = async (enrollmentId) => {
    setMessage('');
    setError('');

    // Confirmação com o usuário
    if (!window.confirm("Você tem certeza que quer cancelar esta matrícula?")) {
      return;
    }

    try {
      const response = await api.delete(`/api/v1/enrollments/${enrollmentId}/cancel`);
      setMessage(response.data); // "Enrollment successfully canceled."
      // Atualiza a lista de matrículas após o cancelamento
      fetchMyEnrollments();
    } catch (err) {
      setError(getErrorMessage(err, 'Erro ao cancelar matrícula.'));
    }
  };

  return (
    <div className="module-container">
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      {/* Card "Gerenciar por ID" foi removido */}

      <h3>Minhas Matrículas (Semestre Atual)</h3>

      {loading && <p>Carregando matrículas...</p>}

      {!loading && myEnrollments.length > 0 && (
        <table className="course-table">
          <thead>
            <tr>
              <th>Cód. da Matrícula</th>
              <th>Cód. do Curso</th>
              <th>Nome do Curso</th>
              <th>Créditos</th>
              <th>Status</th>
              <th>Ação</th>
            </tr>
          </thead>
          <tbody>
            {myEnrollments.map((enrollment) => (
              <tr key={enrollment.id}>
                <td>{enrollment.id}</td>
                <td>{enrollment.course.code}</td>
                <td>{enrollment.course.name}</td>
                <td>{enrollment.course.credits}</td>
                <td>{enrollment.canceled ? "Cancelada" : "Ativa"}</td>
                <td>
                  {!enrollment.canceled && (
                    <button
                      onClick={() => handleCancel(enrollment.id)}
                      className="danger-button"
                    >
                      Cancelar
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ManageEnrollments;