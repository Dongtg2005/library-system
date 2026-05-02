import React, { Fragment } from 'react';
import { Menu, Transition } from '@headlessui/react';
import { useTranslation } from '../context/LanguageContext';

const Dropdown = ({ button, items = [] }) => {
  const { t } = useTranslation();
  
  return (
    <Menu as="div" className="relative inline-block text-left">
      <Menu.Button as={Fragment}>{button}</Menu.Button>
      <Transition
        as={Fragment}
        enter="transition ease-out duration-150"
        enterFrom="opacity-0 scale-95"
        enterTo="opacity-100 scale-100"
        leave="transition ease-in duration-100"
        leaveFrom="opacity-100 scale-100"
        leaveTo="opacity-0 scale-95"
      >
        <Menu.Items className="absolute right-0 mt-3 w-56 origin-top-right rounded-2xl border border-slate-200 bg-white p-2 shadow-2xl dark:border-slate-700 dark:bg-slate-900">
          {items.length === 0 ? (
            <Menu.Item>
              <div className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-medium text-slate-400 dark:text-slate-500">
                <span>{t('dropdown.noOptions')}</span>
              </div>
            </Menu.Item>
          ) : (
            items.map((item) => (
              <Menu.Item key={item.label}>
                {({ active }) => (
                  <button
                    onClick={item.onClick}
                    className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-medium transition ${active ? 'bg-slate-100 text-slate-900 dark:bg-slate-800 dark:text-white' : 'text-slate-600 dark:text-slate-300'}`}
                  >
                    {item.icon}
                    <span>{item.label}</span>
                  </button>
                )}
              </Menu.Item>
            ))
          )}
        </Menu.Items>
      </Transition>
    </Menu>
  );
};

export default Dropdown;
