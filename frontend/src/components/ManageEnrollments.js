import React, { useState, useEffect } from 'react';
import api from '../services/api';

const ManageEnrollments = () => {
  const [myEnrollments, setMyEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

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

  useEffect(() => {
    fetchMyEnrollments();
  }, []);

  const handleCancel = async (enrollmentId) => {
    setMessage('');
    setError('');

    if (!window.confirm("Você tem certeza que quer cancelar esta matrícula?")) {
      return;
    }

    try {
      const response = await api.delete(`/api/v1/enrollments/${enrollmentId}/cancel`);
      setMessage(response.data);
      fetchMyEnrollments();
    } catch (err) {
      setError(getErrorMessage(err, 'Erro ao cancelar matrícula.'));
    }
  };

  return (
    <div className="module-container">
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

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