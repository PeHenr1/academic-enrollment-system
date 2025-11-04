import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Register = () => {
  const [name, setName] = useState('');
  const [lastname, setLastname] = useState('');
  const [studentId, setStudentId] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [error, setError] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== passwordConfirm) {
      setError('As senhas não conferem.');
      return;
    }

    const userData = {
      name,
      lastname,
      email,
      password,
      studentId
    };

    try {
      const success = await register(userData);
      if (success) {
        navigate('/login');
      } else {
        setError('Erro ao tentar registrar. Verifique os dados.');
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Erro ao registrar. Tente novamente.'));
    }
  };

  return (
    <div className="form-container login-register">
      <h2>Registro</h2>
      <form onSubmit={handleSubmit}>
        {error && <p className="error">{error}</p>}

        <div>
          <label>Nome:</label>
          <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div>
          <label>Sobrenome:</label>
          <input type="text" value={lastname} onChange={(e) => setLastname(e.target.value)} required />
        </div>
        <div>
          <label>ID do Estudante (RA):</label>
          <input type="text" value={studentId} onChange={(e) => setStudentId(e.target.value)} required />
        </div>
        <div>
          <label>Email (Login):</label>
          <input type="text" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div>
          <label>Senha:</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>

        {/* --- 4. CAMPO DE CONFIRMAÇÃO ADICIONADO --- */}
        <div>
          <label>Confirmar Senha:</label>
          <input type="password" value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)} required />
        </div>

        <button type="submit">Registrar</button>
      </form>
      <p style={{ textAlign: 'center', marginTop: '15px' }}>
        Já tem uma conta? <Link to="/login">Faça login</Link>
      </p>
    </div>
  );
};

export default Register;