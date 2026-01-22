import React from 'react';
const filters = ["All", "Normal", "Overweight", "Obese"];

const Filter = () => {
  return (
    <div className="filters">
      {filters.map((filter) => (
        <button key={filter} className="filter-btn">
          {filter}
        </button>
      ))}
    </div>
  );
};

export default Filter;
