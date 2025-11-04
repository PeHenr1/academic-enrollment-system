import React, { useState, useEffect, useMemo } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useDebounce } from '../hooks/useDebounce';

const Courses = () => {
  const { user } = useAuth();
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [filterName, setFilterName] = useState('');
  const [filterShift, setFilterShift] = useState('');

  const [selectedCourses, setSelectedCourses] = useState(new Set());
  const [enrollError, setEnrollError] = useState('');
  const [enrollSuccess, setEnrollSuccess] = useState('');

  const debouncedName = useDebounce(filterName, 300);
  const debouncedShift = useDebounce(filterShift, 300);

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

  useEffect(() => {
    setLoading(true);
    setError('');

    api.get('/api/v1/courses/filter', {
      params: {
        name: debouncedName || null,
        shift: debouncedShift || null,
      },
    })
    .then(response => {
      setCourses(response.data);
    })
    .catch(err => {
      setError(getErrorMessage(err, 'Falha ao carregar cursos.'));
    })
    .finally(() => {
      setLoading(false);
    });
  }, [debouncedName, debouncedShift]);

  const handleSelectCourse = (courseCode) => {
    setSelectedCourses(prevSelected => {
      const newSelected = new Set(prevSelected);
      if (newSelected.has(courseCode)) {
        newSelected.delete(courseCode);
      } else {
        newSelected.add(courseCode);
      }
      return newSelected;
    });
  };

  const handleEnrollSubmit = async () => {
    setEnrollError('');
    setEnrollSuccess('');

    if (selectedCourses.size === 0) {
      setEnrollError('Você deve selecionar ao menos um curso.');
      return;
    }

    const courseCodesList = Array.from(selectedCourses);

    try {
      const response = await api.post(`/enrollments/enroll/${user.studentId}`, courseCodesList);
      setEnrollSuccess(response.data);
      setSelectedCourses(new Set());
    } catch (err) {
      setEnrollError(getErrorMessage(err, 'Erro ao realizar matrícula.'));
    }
  };

  const selectedCourseIds = useMemo(() => Array.from(selectedCourses), [selectedCourses]);

  return (
    <div className="module-container">
      <h2>Cursos Ofertados</h2>

      <div className="filter-form">
        <input
          type="text"
          placeholder="Nome do curso"
          value={filterName}
          onChange={(e) => setFilterName(e.target.value)}
        />
        <input
          type="text"
          placeholder="Turno (Ex: Night)"
          value={filterShift}
          onChange={(e) => setFilterShift(e.target.value)}
        />
      </div>

      {loading && <p>Carregando...</p>}
      {error && <p className="error">{error}</p>}

      <table className="course-table">
        <thead>
          <tr>
            <th>Selecionar</th>
            <th>Código</th>
            <th>Nome</th>
            <th>Créditos</th>
            <th>Vagas</th>
            <th>Turno</th>
          </tr>
        </thead>
        <tbody>
          {courses.map((course) => (
            <tr key={course.id}>
              <td>
                <input
                  type="checkbox"
                  checked={selectedCourses.has(course.code)}
                  onChange={() => handleSelectCourse(course.code)}
                  // --- A PROPRIEDADE 'disabled' FOI REMOVIDA ---
                />
              </td>
              <td>{course.code}</td>
              <td>{course.name}</td>
              <td>{course.credits}</td>
              <td>{course.availableSeats}</td>
              <td>{course.shift}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="enrollment-footer">
        {enrollSuccess && <p className="success">{enrollSuccess}</p>}
        {enrollError && <p className="error">{enrollError}</p>}

        <button
          onClick={handleEnrollSubmit}
          disabled={selectedCourseIds.length === 0}
        >
          Matricular em {selectedCourseIds.length} curso(s)
        </button>
      </div>
    </div>
  );
};

export default Courses;